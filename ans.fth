: FIRST_COLON ;

: IMMEDIATE $Si ;

: \ 10 $Sp__ ; immediate
: ( 41 $Sp__ ; immediate

\ Stack manipulation primitives

: DROP ( x -- ) $_ ;
: DUP ( x -- x x ) $d ;
: OVER ( x1 x2 -- x1 x2 x1 ) $o ;
: SWAP ( x1 x2 -- x2 x1 ) $s ;
: ROT ( x1 x2 x3 -- x2 x1 x3 ) $r ;
: NIP ( x1 x2 -- x2 ) $n ;
: PICK ( xu..x1 x0 u -- xu..x1 x0 xu ) $p ;

: >R ( x -- ) ( R: -- x ) $( ;
: R> ( -- x ) ( R: x -- ) $) ;

\ Other stack manipulation words

: -ROT ( a b c -- c a b ) rot rot ;
: 3DUP ( a b c -- a b c a b c ) 2 pick 2 pick 2 pick ;
: 2DROP ( a b -- ) drop drop ;
: TUCK ( a b -- b a b ) swap over ;

\ Arithmetic primitives

: + $+ ;
: - $- ;
: * $* ;
: / $/ ;
: MOD $% ;

: 1+ ( n1 | u1 -- n2 | u2 ) 1 + ;
: 1- ( n1 | u1 -- n2 | u2 ) 1 - ;

: BASE ( -- a-addr ) 20 ; 

\ Comparison primitives

: < $< ;
: = $= ;
: > $> ;
: 0= $0 ;
: 0< ( n -- flag ) 0 < ;
: 0> ( n -- flag ) 0 > ;
: 0<> ( x -- flag ) 0= 0= ;

\ Logical primitives

: AND $& ;
: OR $| ;
: XOR $^ ;
: INVERT $~ ;

\ Memory manipulation primitives

: ! ( x a-addr -- ) $! ;
: @ ( a-addr -- x ) $@ ;
: C! ( char c-addr -- ) $; ;
: C@ ( c-addr -- char ) $: ;

: CELL ( -- n ) $c ;
: CELL+ ( a-addr1 -- a-addr2) cell + ;
: CELLS ( n -- n ) cell * ;

: HERE ( -- addr ) $h ;
: ALLOT ( n -- ) $a ;
: ALIGN ( -- ) $g ;
: ALIGNED ( addr -- a-addr ) cell 1- dup invert swap rot + and ;

: C, ( char -- ) here 1 allot c! ;
: , ( x -- ) here cell allot ! ;

\ Input/Output

: EMIT ( x -- ) $Te ;
: KEY ( -- char ) $Tk ;
: TYPE ( c-addr u -- ) $Tt ;
: ACCEPT ( c-addr +n1 -- +n2 ) $Ta ;

: . ( n -- ) $T. ;

: BL ( -- char ) 32 ;
: CR ( -- ) 10 emit ;

\ System variables 

: LATEST 12 @ ;

\ Useful stack manipulation words

: <= ( a b -- f , true if a <= b ) > 0= ;
: >= ( a b -- f , true if a >= b ) < 0= ;

: NOT ( n -- !n , logical negation ) 0= ;
: NEGATE ( n -- n ) 0 swap - ;

: PARSE ( char "ccc<char>" -- c-addr u ) $Sp ;
: PARSE-NAME ( "<spaces>name<space>" -- c-addr u ) $Sn ;
: FIND-NAME ( c-addr u -- c-addr u nt ) $Sf ;

\ Word related words

: SEE $Ss ;
: NAME>STRING ( nt -- c-addr u ) 12 + dup 1 - c@ ;
: NAME>INTERPRET ( nt -- xt ) 4 + @ ; 
: ' ( "<spaces>name" -- xt ) parse-name find-name name>interpret nip nip ;

: PAD ( -- addr ) here 128 + ;

: EXECUTE $i ;
: BYE $q ;

: ] $S) ; immediate
: [ $S( ; immediate

: >MARK $f ;
: >RESOLVE $b ;
: 0BRANCH $z ;
: JUMP $j ;
: QUOTATION $[ ;

: LITERAL $l ;

: POSTPONE $SnSfnnl$S$c ; immediate

: IF >mark postpone 0branch ; immediate
: ELSE >mark swap postpone jump >resolve ; immediate
: THEN >resolve ; immediate

: BEGIN here ; immediate
\ It should be a 16 bit literal always (that's why I used >mark)
: UNTIL here - 3 - literal postpone 0branch ; immediate

: STATE $S? ;

: ABS ( n -- u ) dup 0< if negate then ;

: RECURSE $Sr ; immediate
