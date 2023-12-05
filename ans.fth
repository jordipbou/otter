\  : TRACE 1 $v ;
\  : UNTRACE 0 $v ;
\  
\  : [: ${ ;
\  : ;] $} ;
\  
\  \ Stack primitives
\  
\  : DROP $_ ;
\  : DUP $d ;
\  : OVER $o ;
\  : SWAP $s ;
\  : ROT $r ;
\  
\  \ Stack words
\  : NIP swap drop ;
\  : 2DUP over over ;
\  : -ROT rot rot ;
\  : TUCK swap over ;
\  
\  : >R $t ;
\  : R> $f ;
\  : R@ $y ;
\  
\  \ Return stack words
\  
\  : 2>R swap >r >r ;
\  : 2R> r> r> swap ;
\  : 2R@ r> r> dup >r >r swap ;
\  
\  \ Arithmetic primitives
\  
\  : + $+ ;
\  : - $- ;
\  : * $* ;
\  : / $/ ;
\  : MOD $% ;
\  
\  \ Arithmetic words
\  
\  : 1+ 1 + ;
\  : 1- 1 - ;
\  
\  : < $< ;
\  : = $= ;
\  : > $> ;
\  : 0= $0 ;
\  
\  : U< $u< ;
\  
\  : AND $& ;
\  : OR $| ;
\  : XOR $^ ;
\  : INVERT $~ ;
\  
\  : LSHIFT $( ;
\  : RSHIFT $) ;
\  
\  : C! $, ;
\  : C@ $. ;
\  : W! $; ;
\  : W@ $: ;
\  : ! $! ;
\  : @ $@ ;
\  
\  : CELL $c ;
\  
\  : CELL+ cell + ;
\  : CELLS cell * ;
\  
\  : HERE $h ;
\  : ALLOT $a ;
\  : ALIGN $g ;
\  
\  : C, here c! 1 allot ;
\  : W, here w! 2 allot ;
\  : , here ! cell allot ;
\  
\  : DUMP $` ;
\  
\  : EMIT $E ;
\  : KEY $K ;
\  
\  : EXECUTE $i ;
\  : 0BRANCH $? ;
\  : BRANCH $j ;
\  : BYE $q ;
\  : EXIT $\ ;
\  
\  : DICT $b ;
\  : DICT-SIZE dict 0 + ;
\  : LATEST dict-size cell + ;
\  : OUTER latest cell + ;
\  : STATE outer cell + ;
\  : IBUF state cell + ;
\  : >IN ibuf 256 + ;
\  : ILEN >in cell + ;
\  
\  : SOURCE ibuf ilen @ ;
\  
\  : NAME>INTERPRET cell + @ ;
\  : NAME>FLAGS cell + cell + ;
\  : NAME>STRING cell + cell + 1 + dup c@ swap 1 + swap ;
\  
\  : FLAG-IMMEDIATE 2 ;
\  : IMMEDIATE latest @ name>flags dup c@ flag-immediate or swap c! ;
\  
\  : >MARK 50 c, here 0 w, ; 
\  : >RESOLVE dup here swap - 3 - swap w! ; 
\  : <MARK here ;
\  : <RESOLVE 50 c, here - 3 - w, ;
\  
\  : IF >mark $$? ; immediate
\  : ELSE >mark $$j swap >resolve ; immediate
\  : THEN >resolve ; immediate
\  
\  : BEGIN <mark ; immediate
\  : AGAIN <resolve $$j ; immediate
\  : WHILE $$0 >mark $$? swap ; immediate
\  : REPEAT <resolve $$j >resolve ; immediate
\  : UNTIL $$0 <resolve $$? ; immediate
\  
\  : /STRING swap over - -rot + swap ;
\  
\  : MIN 2dup > if swap then drop ;
\  : MAX 2dup < if swap then drop ;
\  
\  : BL 32 ;
\  : ISSPACE? bl 1+ u< ;
\  : ISNOTSPACE? isspace? 0= ;
\  : XT-SKIP 
\  	>r 
\  	begin 
\  		dup 
\  	while 
\  		over c@ r@ execute 
\  	while 
\  		1 /string 
\  	repeat then 
\  	r> drop ;
\  
\  : PARSE-NAME 
\  	source >in @ /string 
\  	[: bl 1+ u< ;] xt-skip over >r 
\  	[: bl 1+ u< 0= ;]  xt-skip
\  	2dup 1 min + source drop - >in !
\  	drop r> tuck - ;
\  
\  : TYPE begin over c@ emit swap 1+ swap 1- dup until drop drop ;
