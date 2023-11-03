: FIRST_COLON ;

: DROP $_ ;
: DUP $d ;
: OVER $o ;
: SWAP $s ;
: ROT $r ;
: NIP $n ;
: PICK $p ;

: >R $( ;
: R> $) ;

: + $+ ;
: - $- ;
: * $* ;
: / $/ ;
: MOD $% ;

: 1+ 1 + ;

: < $< ;
: = $= ;
: > $> ;
: 0= $0 ;

: AND $& ;
: OR $| ;
: XOR $^ ;
: INVERT $~ ;

: ! $! ;
: @ $@ ;
: C! $; ;
: C@ $: ;

: EXECUTE $x ;

: TYPE $t ;

: CELL $c ;
: CELLS CELL * ;

: PARSE $Sp ;
: IMMEDIATE $Si ;

: 'NT $SnSfnn ;
: NT>NAME $Sn ;

: (   41 parse drop drop ; immediate
( That was the definition for the comment word. )
( Now we can add comments to what we are doing! )
( Note that we are in decimal numeric input mode. )

: \ ( <line> -- , comment out rest of line )
        10 parse drop drop
; immediate

\ *********************************************************************
\ This is another style of comment that is common in Forth.
\ pFORTH - Portable Forth System
\ Based on HMSL Forth
\
\ Author: Phil Burk
\ Copyright 1994 3DO, Phil Burk, Larry Polansky, David Rosenboom
\
\ Permission to use, copy, modify, and/or distribute this
\ software for any purpose with or without fee is hereby granted.
\
\ THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
\ WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
\ WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL
\ THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
\ CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
\ FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
\ CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
\ OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
\ *********************************************************************

\ : COUNT  dup 1+ swap c@ ;
\ 
\ \ Miscellaneous support words
\ : ON ( addr -- , set true )
\         -1 swap !
\ ;
\ : OFF ( addr -- , set false )
\         0 swap !
\ ;
\ 
\ : CELL+ ( n -- n+cell )  cell + ;
\ : CELL- ( n -- n+cell )  cell - ;
\ : CELL* ( n -- n*cell )  cells ;
\ 
\ : CHAR+ ( n -- n+size_of_char ) 1+ ;
\ : CHARS ( n -- n*size_of_char , don't do anything)  ; immediate
\ 
\ \ useful stack manipulation words
\ : -ROT ( a b c -- c a b )
\         rot rot
\ ;
\ : 3DUP ( a b c -- a b c a b c )
\         2 pick 2 pick 2 pick
\ ;
\ : 2DROP ( a b -- )
\         drop drop
\ ;
\ \ : NIP ( a b -- b )
\ \         swap drop
\ \ ;
\ : TUCK ( a b -- b a b )
\         swap over
\ ;
\ 
\ : <= ( a b -- f , true if A <= b )
\         > 0=
\ ;
\ : >= ( a b -- f , true if A >= b )
\         < 0=
\ ;
\ 
\ \ : INVERT ( n -- 1'comp )
\ \     -1 xor
\ \ ;
\ 
\ : NOT ( n -- !n , logical negation )
\         0=
\ ;
\ 
\ : NEGATE ( n -- -n )
\         0 swap -
\ ;
\ 
\ : DNEGATE ( d -- -d , negate by doing 0-d )
\         0 0 2swap d-
\ ;
\ 
\ \ --------------------------------------------------------------------
\ 
\ \ : ID.   ( nfa -- )
\ \     count 31 and type
\ \ ;
\ 
\ \ : DECIMAL   10 base !  ;
\ \ : OCTAL      8 base !  ;
\ \ : HEX       16 base !  ;
\ \ : BINARY     2 base !  ;
\ \ 
\ \ : PAD ( -- addr )
\ \         here 128 +
\ \ ;
\ \ 
\ \ : $MOVE ( $src $dst -- )
\ \         over c@ 1+ cmove
\ \ ;
\ \ : BETWEEN ( n lo hi -- flag , true if between lo & hi )
\ \         >r over r> > >r
\ \         < r> or 0=
\ \ ;
\ \ : [ ( -- , enter interpreter mode )
\ \         0 state !
\ \ ; immediate
\ \ : ] ( -- enter compile mode )
\ \         1 state !
\ \ ;
\ \ 
\ \ : EVEN-UP  ( n -- n | n+1 , make even )  dup 1 and +  ;
\ \ : ALIGNED  ( addr -- a-addr )
\ \         [ cell 1- ] literal +
\ \         [ cell 1- invert ] literal and
\ \ ;
\ \ : ALIGN ( -- , align DP )  dp @ aligned dp ! ;
\ \ : ALLOT ( nbytes -- , allot space in dictionary ) dp +! ( align ) ;
\ \ 
\ \ : C,    ( c -- )  here c! 1 chars dp +! ;
\ \ : W,    ( w -- )  dp @ even-up dup dp !    w!  2 chars dp +! ;
\ \ : , ( n -- , lay into dictionary )  align here !  cell allot ;
\ \ 
