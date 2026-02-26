package paboomi.practica1_compi1_1s_2026.backend.analyzer;

%%
%class Lexer
%unicode
%cup
%line
%column

WHITESPACE = [ \t\r\n]+
ID = [a-zA-Z_][a-zA-Z0-9_]*
INT = [0-9]+
DEC = [0-9]+"."[0-9]+
STRING = \"([^\"\\]|\\.)*\"
HEX = H[0-9A-Fa-f]{6}

%%

{WHITESPACE}           { /* skip */ }
"#".*                  { /* comentario */ }

"INICIO"               { return sym.INICIO; }
"FIN"                  { return sym.FIN; }
"SI"                   { return sym.SI; }
"ENTONCES"             { return sym.ENTONCES; }
"FINSI"                { return sym.FINSI; }
"MIENTRAS"             { return sym.MIENTRAS; }
"HACER"                { return sym.HACER; }
"FINMIENTRAS"          { return sym.FINMIENTRAS; }
"VAR"                  { return sym.VAR; }
"MOSTRAR"              { return sym.MOSTRAR; }
"LEER"                 { return sym.LEER; }

"%"?"DEFAULT"          { return sym.CONF_DEFAULT; }
"%"?"COLOR_TEXTO_SI"       { return sym.CONF_COLOR_TEXTO_SI; }
"%"?"COLOR_SI"             { return sym.CONF_COLOR_SI; }
"%"?"FIGURA_SI"            { return sym.CONF_FIGURA_SI; }
"%"?"LETRA_SI"             { return sym.CONF_LETRA_SI; }
"%"?"LETRA_SIZE_SI"        { return sym.CONF_LETRA_SIZE_SI; }
"%"?"COLOR_TEXTO_MIENTRAS" { return sym.CONF_COLOR_TEXTO_MIENTRAS; }
"%"?"COLOR_MIENTRAS"       { return sym.CONF_COLOR_MIENTRAS; }
"%"?"FIGURA_MIENTRAS"      { return sym.CONF_FIGURA_MIENTRAS; }
"%"?"LETRA_MIENTRAS"       { return sym.CONF_LETRA_MIENTRAS; }
"%"?"LETRA_SIZE_MIENTRAS"  { return sym.CONF_LETRA_SIZE_MIENTRAS; }
"%"?"COLOR_TEXTO_BLOQUE"   { return sym.CONF_COLOR_TEXTO_BLOQUE; }
"%"?"COLOR_BLOQUE"         { return sym.CONF_COLOR_BLOQUE; }
"%"?"FIGURA_BLOQUE"        { return sym.CONF_FIGURA_BLOQUE; }
"%"?"LETRA_BLOQUE"         { return sym.CONF_LETRA_BLOQUE; }
"%"?"LETRA_SIZE_BLOQUE"    { return sym.CONF_LETRA_SIZE_BLOQUE; }

"ELIPSE"               { return sym.FIG_ELIPSE; }
"CIRCULO"              { return sym.FIG_CIRCULO; }
"PARALELOGRAMO"        { return sym.FIG_PARALELOGRAMO; }
"RECTANGULO"           { return sym.FIG_RECTANGULO; }
"ROMBO"                { return sym.FIG_ROMBO; }
"RECTANGULO_REDONDEADO" { return sym.FIG_RECTANGULO_REDONDEADO; }

"ARIAL"                { return sym.FONT_ARIAL; }
"TIMES_NEW_ROMAN"      { return sym.FONT_TIMES_NEW_ROMAN; }
"COMIC_SANS"           { return sym.FONT_COMIC_SANS; }
"VERDANA"              { return sym.FONT_VERDANA; }

"=="    { return sym.IGUALDAD; }
"!="    { return sym.DIFERENTE; }
">="    { return sym.MAYOR_IGUAL; }
"<="    { return sym.MENOR_IGUAL; }
">"     { return sym.MAYOR; }
"<"     { return sym.MENOR; }

"&&"    { return sym.AND; }
"||"    { return sym.OR; }
"!"     { return sym.NOT; }

"+"     { return sym.MAS; }
"-"     { return sym.MENOS; }
"*"     { return sym.POR; }
"/"     { return sym.DIV; }

"="     { return sym.ASIG; }
"("     { return sym.PAR_IZQ; }
")"     { return sym.PAR_DER; }
","     { return sym.COMA; }
"|"     { return sym.PIPE; }

"%%%%" { return sym.SEP_SECCIONES; }

{DEC}    { yylval.sval = yytext(); return sym.DECIMAL; }
{INT}    { yylval.sval = yytext(); return sym.ENTERO; }
{HEX}    { yylval.sval = yytext(); return sym.HEX; }
{STRING} { yylval.sval = yytext(); return sym.CADENA; }
{ID}     { yylval.sval = yytext(); return sym.ID; }

. { System.err.println("Error léxico en línea " + (yyline+1) + ", columna " + (yycolumn+1) + ": '" + yytext() + "'"); }