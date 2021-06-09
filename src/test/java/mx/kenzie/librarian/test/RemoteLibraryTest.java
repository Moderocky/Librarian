package mx.kenzie.librarian.test;

import mx.kenzie.librarian.Library;
import mx.kenzie.librarian.RemoteLibrary;
import org.junit.Test;

import java.rmi.Remote;
import java.rmi.RemoteException;

public class RemoteLibraryTest {
    
    @Test
    public void simple() {
        final Library<Remote> library = new RemoteLibrary();
        //region Stubs
        interface Jeremy extends Remote {
            int a() throws RemoteException;
            
            int b() throws RemoteException;
        }
        
        interface Alice {
            int a();
            
            int b();
        }

        class Bob implements Jeremy {
            public int a() throws RemoteException {
                return 1;
            }
    
            public int b() throws RemoteException {
                return 2;
            }
    
        }
        //endregion
        assert library.register(new Bob());
        final Alice alice = library.lookFor(Alice.class);
        assert alice != null;
        assert alice.a() == 1;
        assert alice.b() == 2;
        assert library.empty() > 0;
    }
    
}
