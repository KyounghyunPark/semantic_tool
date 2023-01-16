package kr.re.etri.semanticanalysis;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import kr.re.etri.semanticanalysis.request.DittoRequest;

public class WatchFolder {
	
	@SuppressWarnings("unused")
	public void watchFolder(DittoRequest dittoRequest) {
		Path path = Path.of(System.getProperty("user.dir") + "/mapping_result");
		
        System.out.println("Watching path: " + path);

        // We obtain the file system of the Path
        FileSystem fs = path.getFileSystem();

        // We create the new WatchService using the new try() block
        try (WatchService service = fs.newWatchService()) {

            // We register the path to the service
            // We watch for creation events
            path.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            // Start the infinite polling loop
            WatchKey key = null;
            while (true) {
                key = service.take();

                // Dequeueing events
//                Kind<?> kind = null;
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    // Get the type of the event
//                    kind = watchEvent.kind();
//                    if (StandardWatchEventKinds.OVERFLOW == kind) {
//                        continue; // loop
//                    } else if (StandardWatchEventKinds.ENTRY_CREATE == kind) {
//                        // A new Path was created
//                        Path newPath = ((WatchEvent<Path>) watchEvent)
//                                .context();
//                        // Output
//                        
//                        
//                        System.out.println("New path created: " + newPath);
//                    } else if (StandardWatchEventKinds.ENTRY_MODIFY == kind) {
//                        // modified
//                        Path newPath = ((WatchEvent<Path>) watchEvent)
//                                .context();
//                        // Output
//                        System.out.println("New path modified: " + newPath);
//                    }
                    
                    dittoRequest.readMappingResult();
                }

                if (!key.reset()) {
                    break; // loop
                }
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
	}
}
