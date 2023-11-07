# Bytecodes

	  (< 32) -> noop
    (SPACE) -> noop
    ! -> store cell (32/64 bit)
    " -> raw string literal
    # -> raw number literal
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
    : -> fetch word/short (16 bit)
    ; -> store word/short (16 bit)
    < -> less than
    = -> equal
    > -> greater than
    ? -> 
    @ -> fetch cell (32/64 bit)
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
    \ -> fetch int/long (32 bit)
    ] -> return (quotation end)
    ^ -> xor
    _ -> drop
    ` -> store int/long (32 bit)
    a -> allot
    b -> block bytecodes
      f -> freeze point (non alloc bottom)
        @ -> get
        ! -> set
      l -> limit point (non alloc top)
        @ -> get
        ! -> set
      s -> size/capacity
        @ -> get
        ! -> set
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
		m -> compare strings
    n -> nip
    o -> over
    p -> pick
    q -> bye (set error -256)
    r -> rot
    s -> swap
    t -> throw (set error)
    u -> untrace
    v -> view traces
    w -> word bytecodes
      : -> colon
      ; -> semicolon
      c -> create
      l -> latest
        @ -> get
        ! -> set
      f -> flags
        @ -> get
        ! -> set
      n -> name (c-addr u)
        @ -> get
        ! -> set
      p -> previous (get)
    x -> context bytecodes
      @ -> get current context address/object
      b -> block address
        @ -> get
        ! -> set
      e -> error code
        @ -> get
        ! -> set
      i -> instruction pointer
        @ -> get
        ! -> set
      r -> return stack pointer
        @ -> get
        ! -> set
      s -> data stack pointer
        @ -> get
        ! -> set
    y -> 
    z -> zero branch (jump if zero)
    { -> raw quotation literal
    | -> or
    } -> end raw quotation (return)
    ~ -> invert
