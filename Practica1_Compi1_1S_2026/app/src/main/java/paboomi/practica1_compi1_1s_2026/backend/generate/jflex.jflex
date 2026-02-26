package paboomi.practica1_compi1_1s_2026.backend.analyzer;

%%
%public
%class Lexer
%unicode
%cup
%line
%column
%ignorecase

%{
   /*---------------------------------------------
        Codigo para el manejo de errores
    -----------------------------------------------*/

    private List<String> errorList;
    
    public List<String> getLexicalErrors(){
        return this.errorList;
    }

     /*-----------------------------------------------
              Codigo para el parser
        -------------------------------------------------*/
        private Symbol symbol(int type){
            return new Symbol(type, yyline+1, yycolumn+1);
        }

        private Symbol symbol(int type, Object value){
            return new Symbol(type, yyline+1, yycolumn+1, value);
        }

         private void error(String message){
         errorList.add("Error en la linea: " + (yyline+1) + ", columna: " + (yycolumn+1) + " : " + message);
    }
%}

WHITESPACE = [ \t\r\n]+
ID = [a-zA-Z_][a-zA-Z0-9_]*
INT = [0-9]+
DEC = [0-9]+"."[0-9]+
STRING = \"([^\"\\]|\\.)*\"
HEX = H[0-9A-Fa-f]{6}

%%

{WHITESPACE}           { /* skip */ }
"#".*                  { /* comentario */ }

"INICIO"               { return symbol(sym.INICIO); }
"FIN"                  { return symbol(sym.FIN); }
"SI"                   { return symbol(sym.SI); }
"ENTONCES"             { return symbol(sym.ENTONCES); }
"FINSI"                { return symbol(sym.FINSI); }
"MIENTRAS"             { return symbol(sym.MIENTRAS); }
"HACER"                { return symbol(sym.HACER); }
"FINMIENTRAS"          { return symbol(sym.FINMIENTRAS); }
"VAR"                  { return symbol(sym.VAR); }
"MOSTRAR"              { return symbol(sym.MOSTRAR); }
"LEER"                 { return symbol(sym.LEER); }

"%"?"DEFAULT"          { return symbol(sym.CONF_DEFAULT); }
"%"?"COLOR_TEXTO_SI"       { return symbol(sym.CONF_COLOR_TEXTO_SI); }
"%"?"COLOR_SI"             { return symbol(sym.CONF_COLOR_SI); }
"%"?"FIGURA_SI"            { return symbol(sym.CONF_FIGURA_SI); }
"%"?"LETRA_SI"             { return symbol(sym.CONF_LETRA_SI); }
"%"?"LETRA_SIZE_SI"        { return symbol(sym.CONF_LETRA_SIZE_SI); }
"%"?"COLOR_TEXTO_MIENTRAS" { return symbol(sym.CONF_COLOR_TEXTO_MIENTRAS); }
"%"?"COLOR_MIENTRAS"       { return symbol(sym.CONF_COLOR_MIENTRAS); }
"%"?"FIGURA_MIENTRAS"      { return symbol(sym.CONF_FIGURA_MIENTRAS); }
"%"?"LETRA_MIENTRAS"       { return symbol(sym.CONF_LETRA_MIENTRAS); }
"%"?"LETRA_SIZE_MIENTRAS"  { return symbol(sym.CONF_LETRA_SIZE_MIENTRAS); }
"%"?"COLOR_TEXTO_BLOQUE"   { return symbol(sym.CONF_COLOR_TEXTO_BLOQUE); }
"%"?"COLOR_BLOQUE"         { return symbol(sym.CONF_COLOR_BLOQUE); }
"%"?"FIGURA_BLOQUE"        { return symbol(sym.CONF_FIGURA_BLOQUE); }
"%"?"LETRA_BLOQUE"         { return symbol(sym.CONF_LETRA_BLOQUE); }
"%"?"LETRA_SIZE_BLOQUE"    { return symbol(sym.CONF_LETRA_SIZE_BLOQUE); }

"ELIPSE"               { return symbol(sym.FIG_ELIPSE); }
"CIRCULO"              { return symbol(sym.FIG_CIRCULO); }
"PARALELOGRAMO"        { return symbol(sym.FIG_PARALELOGRAMO); }
"RECTANGULO"           { return symbol(sym.FIG_RECTANGULO); }
"ROMBO"                { return symbol(sym.FIG_ROMBO); }
"RECTANGULO_REDONDEADO" { return symbol(sym.FIG_RECTANGULO_REDONDEADO); }

"ARIAL"                { return symbol(sym.FONT_ARIAL); }
"TIMES_NEW_ROMAN"      { return symbol(sym.FONT_TIMES_NEW_ROMAN); }
"COMIC_SANS"           { return symbol(sym.FONT_COMIC_SANS); }
"VERDANA"              { return symbol(sym.FONT_VERDANA); }

"=="    { return symbol(sym.IGUALDAD); }
"!="    { return symbol(sym.DIFERENTE); }
">="    { return symbol(sym.MAYOR_IGUAL); }
"<="    { return symbol(sym.MENOR_IGUAL); }
">"     { return symbol(sym.MAYOR); }
"<"     { return symbol(sym.MENOR); }

"&&"    { return symbol(sym.AND); }
"||"    { return symbol(sym.OR); }
"!"     { return symbol(sym.NOT); }

"+"     { return symbol(sym.MAS); }
"-"     { return symbol(sym.MENOS); }
"*"     { return symbol(sym.POR); }
"/"     { return symbol(sym.DIV); }

"="     { return symbol(sym.ASIG); }
"("     { return symbol(sym.PAR_IZQ); }
")"     { return symbol(sym.PAR_DER); }
","     { return symbol(sym.COMA); }
"|"     { return symbol(sym.PIPE); }

"%%%%" { return symbol(sym.SEP_SECCIONES); }

{DEC}    { return symbol(sym.DECIMAL, yytext()); }
{INT}    { return symbol(sym.ENTERO, yytext()); }
{HEX}    { return symbol(sym.HEX, yytext()); }
{STRING} { return symbol(sym.CADENA, yytext()); }
{ID}     { return symbol(sym.ID, yytext()); }

. { System.err.println("Error léxico en línea " + (yyline+1) + ", columna " + (yycolumn+1) + ": '" + yytext() + "'"); }