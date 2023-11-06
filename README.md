# Bytecodes

	  (< 32) -> noop
    (SPACE) -> noop
    ! -> store cell
    " -> string literal
    # -> number literal
    $ -> compile next byte
    % -> modulo
    & -> and
    ' -> byte literal (8 bit)
    ( -> push to R
    ) -> pop from R
    * -> multiplication
    + -> addition
    , -> store byte (8 bit)
    - -> substraction
    . -> fetch byte (8 bit)
    / -> division
    0 -> equals to zero comparison (0=)
    1 -> literal 1
    2 -> short literal (16 bit)
    3 ->
    4 -> int literal (32 bit)
    5 ->
    6 ->
    7 ->
    8 -> long literal (64 bit)
    9 ->
    : -> fetch short (16 bit)
    ; -> store short (16 bit)
    < -> less than
    = -> equal
    > -> greater than
    ? -> 
    @ -> fetch cell
		A -> extension
		B -> extension
		C -> extension
		D -> extension
		E -> extension
		F -> extension
		G -> extension
		H -> extension
		I -> extension
		J -> extension
		K -> extension
		L -> extension
		M -> extension
		N -> extension
		O -> extension
		P -> extension
		Q -> extension
		R -> extension
		Q -> extension
		S -> extension
		T -> extension
		U -> extension
		V -> extension
		W -> extension
		X -> extension
		Y -> extension
		Z -> extension
    [ -> quotation (push ip and jump)
    \ -> fetch int (32 bit)
    ] -> return (quotation end)
    ^ -> xor
    _ -> drop
    ` -> store int (32 bit)
    a -> allot
    b -> 
		c -> sizeof cell
    d -> dup
    e -> 
    f -> 
    g -> align
    h -> here
    i -> interpret/execute/call
    j -> jump
    k -> 
    l -> compile number literal
		m -> compare
    n -> nip
    o -> over
    p -> pick
    q -> bye (set error -256)
    r -> rot
    s -> swap
    t -> 
    u -> untrace
    v -> view traces
    w -> 
    x -> execute/call
    y -> 
    z -> zero branch (jump if zero)
    { -> start quotation (non nestable)
    | -> or
    } -> end quotation (return)
    ~ -> invert
