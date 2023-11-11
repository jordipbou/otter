: DROP $_ ;
: DUP $d ;
: OVER $o ;
: SWAP $s ;
: ROT $r ;
: NIP $n ;
: PICK $p ;

: >R $( ;
: R> $) ;
: R@ $f ;

: -ROT rot rot ;
: 3DUP 2 pick 2 pick 2 pick ;
: 2DROP drop drop ;
: TUCK swap over ;

: + $+ ;
: - $- ;
: * $* ;
: / $/ ;
: MOD $% ;

: 1+ 1 + ;
: 1- 1 - ;

: < $< ;
: = $= ;
: > $> ;
: 0= $0 ;

: <= > 0= ;
: >= < 0= ;

: 0< 0 < ;
: 0> 0 > ;
: 0<> 0= 0= ;

: AND $& ;
: OR $| ;
: XOR $^ ;
: INVERT $~ ;

: NOT 0= ;
: NEGATE 0 swap - ;

: C! $, ;
: C@ $. ;
: W! $; ;
: W@ $: ;
: ! $! ;
: @ $@ ;

: CELL $c ;
: CELL+ cell + ;
: CELLS cell * ;

: HERE $h ;
: ALLOT $a ;
: ALIGN $g ;
: ALIGNED cell 1- dup invert swap rot + and ;

: C, here 1 allot c! ;
: W, here 2 allot w! ;
: , here cell allot ! ;

: EMIT $E ;

: BL 32 ;
: CR 10 emit ;

: EXECUTE $i ;
: BYE $q ;

: 0BRANCH $z ;
: BRANCH $j ;

: FLAG-IMMEDIATE 2 ;

: LATEST $e ;
: PARSE-NAME $x ;
: FIND-NAME $y ;
: LITERAL $l ;

: IMMEDIATE latest 8 + dup c@ flag-immediate or swap c! ;
: COMPILE, $w ;
: POSTPONE parse-name find-name nip nip literal $$w ; immediate

: >MARK 50 c, here 0 w, ;
: >RESOLVE dup here swap - 3 - swap w! ;

: IF >mark postpone 0branch ; immediate
: THEN >resolve ; immediate
: ELSE >mark postpone branch swap >resolve ; immediate

: ' $D' ;
: SEE $Ds ;