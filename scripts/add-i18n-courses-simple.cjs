const fs = require("fs");
const path = require("path");
const root = path.resolve(__dirname, "..", "frontend/src/app/core/i18n");
const additions = {
  fr: {
    "courses.simpleSubtitle": "Un cours est une note organisée : langue cible, niveau et contenu riche avec tableaux.",
    "courses.new": "Nouveau cours",
    "courses.edit": "Modifier le cours",
    "courses.content": "Contenu du cours",
    "courses.contentHint": "Markdown, titres, images et tableaux",
    "courses.status": "Statut",
    "courses.search": "Rechercher un cours",
    "courses.resetFilters": "Réinitialiser",
    "courses.updated": "Modifié le",
    "courses.emptyContent": "Aucun contenu pour le moment.",
    "courses.saved": "Cours enregistré.",
    "markdown.addRow": "Ajouter une ligne au tableau",
    "markdown.addColumn": "Ajouter une colonne",
    "markdown.removeRow": "Supprimer la ligne du tableau",
    "markdown.removeColumn": "Supprimer la colonne",
  },
  es: {
    "courses.simpleSubtitle": "Un curso es una nota organizada: idioma meta, nivel y contenido rico con tablas.",
    "courses.new": "Nuevo curso",
    "courses.edit": "Editar el curso",
    "courses.content": "Contenido del curso",
    "courses.contentHint": "Markdown, títulos, imágenes y tablas",
    "courses.status": "Estado",
    "courses.search": "Buscar un curso",
    "courses.resetFilters": "Restablecer",
    "courses.updated": "Modificado el",
    "courses.emptyContent": "Aún no hay contenido.",
    "courses.saved": "Curso guardado.",
    "markdown.addRow": "Añadir una fila a la tabla",
    "markdown.addColumn": "Añadir una columna",
    "markdown.removeRow": "Eliminar la fila",
    "markdown.removeColumn": "Eliminar la columna",
  },
  en: {
    "courses.simpleSubtitle": "A course is an organized note: target language, level and rich content with tables.",
    "courses.new": "New course",
    "courses.edit": "Edit course",
    "courses.content": "Course content",
    "courses.contentHint": "Markdown, headings, images and tables",
    "courses.status": "Status",
    "courses.search": "Search a course",
    "courses.resetFilters": "Reset",
    "courses.updated": "Updated on",
    "courses.emptyContent": "No content yet.",
    "courses.saved": "Course saved.",
    "markdown.addRow": "Add a table row",
    "markdown.addColumn": "Add a column",
    "markdown.removeRow": "Remove table row",
    "markdown.removeColumn": "Remove column",
  },
  "zh-CN": {
    "courses.simpleSubtitle": "课程就是一篇有组织的笔记：目标语言、等级和支持表格的富文本内容。",
    "courses.new": "新建课程",
    "courses.edit": "编辑课程",
    "courses.content": "课程内容",
    "courses.contentHint": "Markdown、标题、图片和表格",
    "courses.status": "状态",
    "courses.search": "搜索课程",
    "courses.resetFilters": "重置",
    "courses.updated": "更新于",
    "courses.emptyContent": "暂无内容。",
    "courses.saved": "课程已保存。",
    "markdown.addRow": "添加表格行",
    "markdown.addColumn": "添加列",
    "markdown.removeRow": "删除表格行",
    "markdown.removeColumn": "删除列",
  },
};
for (const [locale, entries] of Object.entries(additions)) {
  const file = path.join(root, `${locale}.ts`);
  const lines = fs.readFileSync(file, "utf8").split(/\r?\n/);
  const close = lines.lastIndexOf("};");
  const current = lines.slice(0, close).join("\n");
  const extra = Object.entries(entries)
    .filter(([key]) => !current.includes(`"${key}":`))
    .map(([key, value]) => `  ${JSON.stringify(key)}: ${JSON.stringify(value)},`);
  lines.splice(close, 0, ...extra);
  fs.writeFileSync(file, lines.join("\n"), "utf8");
  console.log(locale, extra.length, "clés ajoutées");
}
