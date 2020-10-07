function toogleAll(){
       $$('.checkbox').each(
          function(e){
             e.checked = $('checkbox_reference').checked
          }
       );
    }

