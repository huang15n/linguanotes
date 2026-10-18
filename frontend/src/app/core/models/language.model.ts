export interface LanguageLevel {
  id: number;
  code: string;
  label: string;
  position: number;
}

export interface Language {
  id: number;
  name: string;
  code: string;
  levels: LanguageLevel[];
}
