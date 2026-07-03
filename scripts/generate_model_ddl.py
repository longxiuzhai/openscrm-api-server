#!/usr/bin/env python3
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "app" / "models"
OUT = ROOT / "docs" / "schema.sql"

TABLE_COMMENT_FALLBACKS = {
    "Customer": "客户",
    "CustomerInfo": "客户扩展信息",
    "CustomerInfoDisplayRule": "客户信息展示规则",
    "CustomerStaff": "客户与员工关系",
    "CustomerStaffRelationHistory": "客户员工关系历史",
    "Staff": "员工",
    "Department": "部门",
    "MaterialLibTag": "素材库标签",
    "StaffDepartment": "员工部门关系",
    "CustomerStaffTag": "客户员工标签关系",
    "GroupChatTagGroup": "客户群标签组",
    "CorpSetting": "企业设置",
    "GroupChatTag": "客户群标签",
    "TagGroup": "企微客户标签组",
    "Tag": "企微客户标签",
    "ChatMsg": "会话存档消息",
    "ChatMsgContent": "会话存档消息内容",
    "QuickReplyGroup": "快捷回复分组",
    "QuickReply": "快捷回复",
    "QuickReplyDetail": "快捷回复详情",
    "CustomerRemark": "客户自定义字段",
    "RemarkOption": "客户自定义字段选项",
    "CustomerEvent": "客户事件",
    "InternalTag": "内部客户标签",
    "Material": "素材",
    "Remainder": "客户提醒",
    "MassMsg": "客户群发任务",
    "MassMsgStaff": "客户群发员工执行记录",
    "WelcomeMsg": "欢迎语",
    "EventNotify": "事件通知设置",
    "GroupChat": "客户群",
    "GroupChatMember": "客户群成员",
    "GroupChatGroup": "客户群分组",
    "GroupChatAutoJoinCode": "自动拉群码",
    "GroupChatQRCode": "自动拉群码群二维码",
    "GroupChatAutoJoinCodeStaff": "自动拉群码员工关系",
    "GroupChatWelcomeMsg": "客户群欢迎语",
    "GroupChatMassMsg": "客户群群发任务",
    "CustomerStatistic": "客户统计",
    "DataExport": "数据导出任务",
    "ContactWayGroup": "渠道码分组",
    "ContactWay": "渠道码",
    "ContactWaySchedule": "渠道码工作日调度",
    "ContactWayScheduleStaff": "渠道码调度员工关系",
    "ContactWayBackupStaff": "渠道码备份员工关系",
    "ContactWayStaff": "渠道码员工关系",
    "Permission": "权限",
    "Role": "角色",
}

ACRONYMS = {
    "API", "ASCII", "CPU", "CSS", "DNS", "EOF", "GUID", "HTML", "HTTP",
    "HTTPS", "ID", "IP", "JSON", "LHS", "QPS", "RAM", "RHS", "RPC", "SLA",
    "SMTP", "SQL", "SSH", "TLS", "TTL", "UID", "UI", "UUID", "URI", "URL",
    "UTF8", "VM", "XML", "XMPP", "XSRF", "XSS", "QR",
}


def parse_tag(raw):
    tags = {}
    if not raw:
        return tags
    for key, val in re.findall(r'(\w+):"([^"]*)"', raw):
        tags[key] = val
    return tags


def parse_gorm(raw):
    result = {}
    flags = set()
    if not raw:
        return result, flags
    parts = []
    buf = []
    depth = 0
    for ch in raw:
        if ch == ";" and depth == 0:
            parts.append("".join(buf))
            buf = []
            continue
        if ch in "([{":
            depth += 1
        elif ch in ")]}" and depth > 0:
            depth -= 1
        buf.append(ch)
    if buf:
        parts.append("".join(buf))
    for part in parts:
        part = part.strip()
        if not part:
            continue
        if ":" in part:
            key, val = part.split(":", 1)
            result[key.strip()] = val.strip()
        else:
            flags.add(part.strip())
            if re.match(r"^(tinyint|smallint|int|bigint|varchar|char|text|longtext|json|datetime|date|time|decimal|float|double)", part, re.I):
                result.setdefault("type", part)
            elif part.lower() == "json":
                result.setdefault("type", "json")
    return result, flags


def snake(name):
    # Match GORM's common initialism normalization closely enough for fields
    # such as MsgID, ExtStaffIDs and QRCodeURL.
    for acronym in sorted(ACRONYMS, key=len, reverse=True):
        name = name.replace(acronym, acronym.title())
    words = []
    token = ""
    for i, ch in enumerate(name):
        prev = name[i - 1] if i else ""
        nxt = name[i + 1] if i + 1 < len(name) else ""
        boundary = False
        if i and ch.isupper():
            if prev.islower() or prev.isdigit():
                boundary = True
            elif prev.isupper() and nxt.islower():
                boundary = True
        if boundary:
            words.append(token)
            token = ch
        else:
            token += ch
    if token:
        words.append(token)
    return "_".join(w.lower() for w in words)


def table_name(type_name, explicit):
    if type_name in explicit:
        return explicit[type_name]
    return snake(type_name)


def mysql_type(go_type, gorm):
    typ = gorm.get("type", "").strip()
    if typ:
        if ",comment:" in typ:
            typ = typ.split(",comment:", 1)[0]
        if typ == "string":
            if "size" in gorm:
                return f"varchar({gorm['size']})"
            return "longtext"
        return typ
    if go_type in {"time.Time", "*time.Time", "gorm.DeletedAt"}:
        return "datetime(3)"
    if go_type in {"string", "*string"}:
        return "longtext"
    if go_type in {"int", "*int", "int32", "*int32"}:
        return "bigint"
    if go_type in {"uint", "*uint", "uint32", "*uint32"}:
        return "bigint unsigned"
    if go_type in {"int64", "*int64"}:
        return "bigint"
    if go_type in {"uint64", "*uint64"}:
        return "bigint unsigned"
    if go_type in {"uint8", "*uint8", "byte"}:
        return "tinyint unsigned"
    if go_type in {"bool", "*bool"}:
        return "boolean"
    if go_type in {"float64", "*float64"}:
        return "double"
    if go_type in {"float32", "*float32"}:
        return "float"
    if go_type.startswith("constants."):
        if any(x in go_type for x in ["ArrayField", "AutoReplyField", "CustomerRemarkField", "QuickReplyField"]):
            return "json"
        if "DateField" in go_type:
            return "date"
        if "TimeField" in go_type:
            return "bigint"
        return "tinyint"
    return "json"


def extract_structs():
    structs = {}
    for path in MODELS.glob("*.go"):
        text = path.read_text()
        for m in re.finditer(r"type\s+(\w+)\s+struct\s*\{", text):
            name = m.group(1)
            start = m.end()
            depth = 1
            i = start
            while i < len(text) and depth:
                if text[i] == "{":
                    depth += 1
                elif text[i] == "}":
                    depth -= 1
                i += 1
            body = text[start : i - 1]
            comment = ""
            prefix = text[: m.start()].splitlines()
            comment_lines = []
            for line in reversed(prefix):
                stripped = line.strip()
                if stripped.startswith("//"):
                    value = stripped[2:].strip()
                    if value.startswith(name):
                        value = value[len(name) :].strip()
                    comment_lines.append(value)
                    continue
                if stripped == "":
                    if comment_lines:
                        break
                    continue
                break
            if comment_lines:
                comment = " ".join(reversed(comment_lines)).strip()
            structs[name] = (path, body, comment)
    return structs


def extract_table_names():
    explicit = {}
    for path in MODELS.glob("*.go"):
        text = path.read_text()
        for m in re.finditer(r"func\s*\([^)]*\b(\w+)\b[^)]*\)\s+TableName\s*\(\)\s+string\s*\{[^}]*return\s+\"([^\"]+)\"", text, re.S):
            explicit[m.group(1)] = m.group(2)
    return explicit


def extract_auto_migrate():
    text = (MODELS / "models.go").read_text()
    m = re.search(r"AutoMigrate\s*\((.*?)\)\s*\n\s*if err", text, re.S)
    if not m:
        raise RuntimeError("AutoMigrate list not found")
    seen = set()
    names = []
    for name in re.findall(r"&(\w+)\{\}", m.group(1)):
        if name not in seen:
            seen.add(name)
            names.append(name)
    return names


FIELD_RE = re.compile(r"^(\w+)(?:\s+([*\[\]\w\.\{\}]+))?\s*(?:`([^`]*)`)?")


def raw_fields(struct_name, structs, stack=None):
    stack = stack or []
    if struct_name not in structs:
        return []
    if struct_name in stack:
        return []
    _, body, _ = structs[struct_name]
    rows = []
    for line in body.splitlines():
        line = line.strip()
        if not line or line.startswith("//"):
            continue
        line = line.split("//", 1)[0].strip()
        m = FIELD_RE.match(line)
        if not m:
            continue
        field, go_type, raw_tag = m.groups()
        if field in {"func", "type", "var", "const"}:
            continue
        if go_type is None:
            rows.extend(raw_fields(field, structs, stack + [struct_name]))
            continue
        tags = parse_tag(raw_tag or "")
        gorm, flags = parse_gorm(tags.get("gorm", ""))
        if "-" in flags or gorm.get("-") == "":
            continue
        if go_type in {"interface{}", "map[string]interface{}"}:
            continue
        if go_type.startswith("[]") and "type" not in gorm:
            continue
        if ("foreignKey" in gorm or "many2many" in gorm or "references" in gorm) and "type" not in gorm:
            continue
        if go_type in structs and "type" not in gorm:
            continue
        rows.append((field, go_type, tags, gorm, flags))
    return rows


def collect_columns(struct_name, structs):
    columns = []
    for field, go_type, tags, gorm, flags in raw_fields(struct_name, structs):
        name = gorm.get("column") or snake(field)
        col_type = mysql_type(go_type, gorm)
        is_indexed = (
            "index" in flags
            or "uniqueIndex" in flags
            or "unique_index" in flags
            or "unique" in flags
            or "index" in gorm
            or "uniqueIndex" in gorm
            or "unique_index" in gorm
            or "index" in tags.get("sql", "")
        )
        if col_type == "longtext" and is_indexed:
            col_type = "varchar(191)"
        pieces = [f"`{name}`", col_type]
        if "primaryKey" in flags or "primary_key" in flags:
            pieces.append("NOT NULL")
        if "not null" in flags or "notNull" in flags:
            pieces.append("NOT NULL")
        if "default" in gorm:
            default = gorm["default"]
            if default.upper() in {"NULL", "CURRENT_TIMESTAMP"} or re.match(r"^-?\d+(\.\d+)?$", default):
                pieces.append(f"DEFAULT {default}")
            else:
                pieces.append(f"DEFAULT '{default}'")
        if "comment" in gorm:
            comment = gorm["comment"].strip("'").replace("'", "''")
            pieces.append(f"COMMENT '{comment}'")
        columns.append({"name": name, "line": " ".join(pieces), "gorm": gorm, "flags": flags, "tags": tags})
    return columns


def index_defs(columns):
    indexes = {}
    uniques = {}
    primary = []
    for c in columns:
        name = c["name"]
        gorm = c["gorm"]
        flags = c["flags"]
        tags = c["tags"]
        if "primaryKey" in flags or "primary_key" in flags:
            primary.append(name)
        sql_tag = tags.get("sql", "")
        if "index" in sql_tag:
            indexes.setdefault(f"idx_{name}", []).append(name)
        indexed = "index" in flags or "index" in gorm or "index" in sql_tag
        unique_indexed = "uniqueIndex" in flags or "unique_index" in flags or "uniqueIndex" in gorm or "unique_index" in gorm
        if "unique" in flags:
            uniques.setdefault(f"uni_{name}", []).append(name)
        if indexed and "index" in flags:
            indexes.setdefault(f"idx_{name}", []).append(name)
        if "index" in gorm:
            idx_name = gorm["index"] or f"idx_{name}"
            indexes.setdefault(idx_name, []).append(name)
        if unique_indexed:
            idx_name = gorm.get("uniqueIndex") or gorm.get("unique_index") or f"uni_{name}"
            idx_name = idx_name.rstrip(";") or f"uni_{name}"
            uniques.setdefault(idx_name, []).append(name)
    defs = []
    if primary:
        defs.append(f"PRIMARY KEY ({', '.join(f'`{x}`' for x in primary)})")
    for name, cols in uniques.items():
        cols = list(dict.fromkeys(cols))
        defs.append(f"UNIQUE KEY `{name}` ({', '.join(f'`{x}`' for x in cols)})")
    for name, cols in indexes.items():
        cols = list(dict.fromkeys(cols))
        defs.append(f"KEY `{name}` ({', '.join(f'`{x}`' for x in cols)})")
    return defs


def main():
    structs = extract_structs()
    explicit = extract_table_names()
    migrate = extract_auto_migrate()
    missing = [name for name in migrate if name not in structs]
    if missing:
        raise RuntimeError(f"missing structs: {missing}")

    statements = [
        "-- Generated from app/models AutoMigrate structs and GORM tags.",
        "-- Review against a live GORM migration before production use.",
        "SET NAMES utf8mb4;",
        "",
    ]
    covered = []
    for struct in migrate:
        cols = collect_columns(struct, structs)
        tname = table_name(struct, explicit)
        table_comment = (structs[struct][2] or TABLE_COMMENT_FALLBACKS.get(struct, "")).replace("'", "''")
        covered.append((struct, tname, len(cols)))
        if not cols:
            statements.append(f"-- WARNING: {struct} produced no columns.")
            continue
        lines = [c["line"] for c in cols] + index_defs(cols)
        body = ",\n  ".join(lines)
        statements.append(f"DROP TABLE IF EXISTS `{tname}`;")
        suffix = "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        if table_comment:
            suffix += f" COMMENT='{table_comment}'"
        statements.append(f"CREATE TABLE `{tname}` (\n  {body}\n) {suffix};")
        statements.append("")

    OUT.write_text("\n".join(statements))
    print(f"wrote {OUT}")
    print(f"tables: {len(covered)}")
    for struct, tname, ncols in covered:
        print(f"{struct}\t{tname}\t{ncols}")


if __name__ == "__main__":
    main()
