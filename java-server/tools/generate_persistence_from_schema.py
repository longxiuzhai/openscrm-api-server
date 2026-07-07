#!/usr/bin/env python3
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SCHEMA = ROOT / "src" / "main" / "resources" / "db" / "schema.sql"
JAVA_ROOT = ROOT / "src" / "main" / "java"
ENTITY_DIR = JAVA_ROOT / "cn" / "openscrm" / "api" / "persistence" / "entity"
MAPPER_DIR = JAVA_ROOT / "cn" / "openscrm" / "api" / "persistence" / "mapper"


def camel(name):
    parts = name.split("_")
    return parts[0] + "".join(p[:1].upper() + p[1:] for p in parts[1:])


def pascal(name):
    value = camel(name)
    return value[:1].upper() + value[1:]


def java_type(sql_type):
    t = sql_type.lower()
    if t.startswith("bigint"):
        return "Long"
    if t.startswith("int") or t.startswith("tinyint") or t.startswith("smallint") or t.startswith("mediumint"):
        return "Integer"
    if t.startswith("decimal"):
        return "BigDecimal"
    if t.startswith("double"):
        return "Double"
    if t.startswith("float"):
        return "Float"
    if t.startswith("datetime") or t.startswith("timestamp"):
        return "LocalDateTime"
    if t.startswith("date"):
        return "LocalDate"
    if t.startswith("time"):
        return "LocalTime"
    if t.startswith("boolean") or t.startswith("bool"):
        return "Boolean"
    return "String"


def parse_schema():
    text = SCHEMA.read_text()
    tables = []
    pattern = re.compile(
        r"CREATE TABLE `(?P<table>[^`]+)` \(\n(?P<body>.*?)\n\) [^;]*?(?: COMMENT='(?P<comment>[^']*)')?;",
        re.S,
    )
    for match in pattern.finditer(text):
        table = match.group("table")
        body = match.group("body")
        comment = match.group("comment") or table
        columns = []
        primary_keys = []
        for raw in body.splitlines():
            line = raw.strip().rstrip(",")
            col = re.match(r"`(?P<name>[^`]+)`\s+(?P<type>[a-zA-Z0-9(), ]+)(?:\s+.*?COMMENT '(?P<comment>.*?)')?$", line)
            if col:
                columns.append(
                    {
                        "name": col.group("name"),
                        "type": " ".join(col.group("type").split()),
                        "comment": col.group("comment") or "",
                    }
                )
                continue
            pk = re.match(r"PRIMARY KEY \((?P<cols>.*?)\)", line)
            if pk:
                primary_keys = re.findall(r"`([^`]+)`", pk.group("cols"))
        tables.append({"name": table, "comment": comment, "columns": columns, "primary_keys": primary_keys})
    return tables


def field_comment(text):
    if not text:
        return ""
    return "\n    /**\n     * " + text.replace("*/", "* /") + "\n     */"


def write_entity(table):
    class_name = pascal(table["name"]) + "Po"
    imports = {
        "com.baomidou.mybatisplus.annotation.TableField",
        "com.baomidou.mybatisplus.annotation.TableName",
        "lombok.Getter",
        "lombok.Setter",
    }
    types = [java_type(c["type"]) for c in table["columns"]]
    if "BigDecimal" in types:
        imports.add("java.math.BigDecimal")
    if "LocalDate" in types:
        imports.add("java.time.LocalDate")
    if "LocalDateTime" in types:
        imports.add("java.time.LocalDateTime")
    if "LocalTime" in types:
        imports.add("java.time.LocalTime")
    single_pk = table["primary_keys"][0] if len(table["primary_keys"]) == 1 else None
    if single_pk:
        imports.add("com.baomidou.mybatisplus.annotation.TableId")

    lines = [
        "package cn.openscrm.api.persistence.entity;",
        "",
    ]
    for item in sorted(imports):
        lines.append(f"import {item};")
    lines.extend(
        [
            "",
            "/**",
            f" * {table['comment']}",
            " */",
            "@Getter",
            "@Setter",
            f'@TableName("{table["name"]}")',
            f"public class {class_name} {{",
        ]
    )
    for col in table["columns"]:
        name = col["name"]
        jtype = java_type(col["type"])
        fname = camel(name)
        lines.append(field_comment(col["comment"]))
        if name == single_pk:
            lines.append(f'    @TableId("{name}")')
        else:
            lines.append(f'    @TableField("{name}")')
        lines.append(f"    private {jtype} {fname};")
        lines.append("")
    lines.append("}")
    (ENTITY_DIR / f"{class_name}.java").write_text("\n".join(lines))


def write_mapper(table):
    entity_name = pascal(table["name"]) + "Po"
    mapper_name = entity_name + "Mapper"
    lines = [
        "package cn.openscrm.api.persistence.mapper;",
        "",
        "import cn.openscrm.api.persistence.entity." + entity_name + ";",
        "import com.baomidou.mybatisplus.core.mapper.BaseMapper;",
        "",
        f"public interface {mapper_name} extends BaseMapper<{entity_name}> {{",
        "}",
    ]
    (MAPPER_DIR / f"{mapper_name}.java").write_text("\n".join(lines))


def main():
    ENTITY_DIR.mkdir(parents=True, exist_ok=True)
    MAPPER_DIR.mkdir(parents=True, exist_ok=True)
    tables = parse_schema()
    for table in tables:
        write_entity(table)
        write_mapper(table)
    print(f"generated {len(tables)} tables")


if __name__ == "__main__":
    main()
