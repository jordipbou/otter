: DROP $_ ;
: DUP $d ;
: OVER $o ;
: SWAP $s ;
: ROT $r ;
: NIP $n ;
: PICK $p ;

: >R $t ;
: R> $f ;
: R@ $u ;
: U $u ;
: V $v ;
: W $w ;
: X $x ;
: Y $y ;
: Z $z ;

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

: AND $& ;
: OR $| ;
: XOR $^ ;
: INVERT $~ ;

: LSHIFT $( ;
: RSHIFT $) ;

: C! $, ;
: C@ $. ;
: W! $; ;
: W@ $: ;
: ! $! ;
: @ $@ ;

: CELL $c ;

: HERE $h ;
: ALLOT $a ;
: ALIGN $g ;

: DUMP $` ;

: EMIT $E ;
: KEY $K ;

: EXECUTE $i ;
: 0BRANCH $? ;
: BRANCH $j ;
: BYE $q ;
: EXIT $\ ;

: U< $u< ;

: BL 32 ;
: ISSPACE? bl 1+ u< ;
: ISNOTSPACE? isspace? 0= ;
: XT-SKIP >r begin dup while over c@ r@ execute while 1 /string repeat then r> drop ;



: C, here c! 1 allot ;
: W, here w! 2 allot ;
: , here ! cell allot ;





: BLOCK-BASE 0 ;
: BLOCK-SIZE@ block-base 0 + ;
: LATEST@ block-size@ cell + ;
: INTERPRETER@ latest@ cell + ;
: STATE interpreter@ cell + ;
: IBUF@ state cell + ;
: >IN ibuf@ 256 + ;
: ILEN@ >in cell + ;

: SOURCE IBUF@ ILEN@ @ ;

: NAME>INTERPRET cell + @ ;
: NAME>FLAGS@ cell + cell + ;
: NAME>STRING cell + cell + 1 + dup c@ swap 1 + swap ;

: LATEST latest@ @ ;
: FLAG-IMMEDIATE 2 ;
: IMMEDIATE latest name>flags@ dup c@ flag-immediate or swap c! ;

: >MARK 50 c, here 0 w, ; 
: >RESOLVE dup here swap - 3 - swap w! ; 
: <MARK here ;
: <RESOLVE 50 c, here - 3 - w, ;

: IF >mark $$? ; immediate
: ELSE >mark $$j swap >resolve ; immediate
: THEN >resolve ; immediate

: BEGIN <mark ; immediate
: AGAIN <resolve $$j ; immediate
: UNTIL >mark $$?$\ >resolve <resolve $$j ; immediate
: WHILE $$0 >mark $$? swap ; immediate

: TYPE begin over c@ emit swap 1 + swap 1 - dup 0= until ;
