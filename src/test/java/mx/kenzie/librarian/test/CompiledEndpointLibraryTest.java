package mx.kenzie.librarian.test;

import mx.kenzie.librarian.CompiledEndpointLibrary;
import mx.kenzie.librarian.Library;
import org.junit.Test;

public class CompiledEndpointLibraryTest {
    
    @Test
    public void simple() {
        final Library<Object> library = new CompiledEndpointLibrary();
        class Alice {
            public int a() {
                return 1;
            }
            
            public int b() {
                return 2;
            }
        }
        assert library.register(new Alice());
        final Alice alice = library.getResource(Alice.class);
        assert alice != null;
        assert alice.a() == 1;
        assert alice.b() == 2;
    }
    
    @Test
    public void simpleBlind() {
        final Library<Object> library = new CompiledEndpointLibrary();
        assert library.register(new Alice());
        final Bob bob = library.lookFor(Bob.class);
        assert bob != null;
        assert bob.a() == 1;
        assert bob.b() == 2;
    }
    
    @Test
    public void doubleBlind() {
        final Library<Object> library = new CompiledEndpointLibrary();
        assert library.register(new Alice());
        assert library.register(new Jeremy());
        final Bob bob = library.lookFor(Bob.class);
        assert bob != null;
        assert bob.a() == 1;
        assert bob.b() == 2;
        assert bob.c() == 5;
        assert bob.d() == 6;
    }
    
    @Test
    public void repeat() {
        final Library<Object> library = new CompiledEndpointLibrary();
        assert library.register(new Alice());
        final Bob bob = library.lookFor(Bob.class);
        assert bob != null;
        assert bob.a() == 1;
        assert bob.b() == 2;
        final Bob two = library.lookFor(Bob.class);
        assert bob.getClass() == two.getClass();
    }
    
    //region Stubs
    public interface Bob {
        int a();
        
        int b();
        
        int c();
        
        int d();
    }
    
    public static class Alice {
        public int a() {
            return 1;
        }
        
        public int b() {
            return 2;
        }
    }
    
    public static class Jeremy {
        public int c() {
            return 5;
        }
        
        public int d() {
            return 6;
        }
    }
    //endregion
    
}
