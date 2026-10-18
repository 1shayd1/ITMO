%define address 0
%macro colon 2
    %ifstr %1
    	%ifid %2
	    %%top: dq address
	    db %1, 0
	    %2:
	    %define address %%top
	%else
	    %error "Second arg must be identifier"
	%endif
    %else
        %error "First arg must be string"
    %endif
%endmacro
