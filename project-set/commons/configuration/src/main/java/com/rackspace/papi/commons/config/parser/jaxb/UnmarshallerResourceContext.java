package com.rackspace.papi.commons.config.parser.jaxb;

import com.rackspace.papi.commons.config.resource.ConfigurationResource;
import com.rackspace.papi.commons.util.io.RawInputStreamReader;
import com.rackspace.papi.commons.util.pooling.ResourceContext;
import com.rackspace.papi.commons.util.pooling.ResourceContextException;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.slf4j.LoggerFactory;

public class UnmarshallerResourceContext implements ResourceContext<Unmarshaller, Object> {

//   private static boolean log = false;
   private final ConfigurationResource cfgResource;

   public UnmarshallerResourceContext(ConfigurationResource cfgResource) {
      this.cfgResource = cfgResource;
   }

   @Override
   public Object perform(Unmarshaller resource) throws ResourceContextException {
      try {
         // This should not belong in production code. The boolean value that controls this is switchable only at compile time.
         
//         if (log) {
//            LoggerFactory.getLogger(UnmarshallerResourceContext.class).error(
//                    new String(RawInputStreamReader.instance().readFully(cfgResource.newInputStream())));
//         }

         return resource.unmarshal(cfgResource.newInputStream());
      } catch (JAXBException jaxbe) {
         throw new ResourceContextException("Failed to unmarshall resource " + cfgResource.name()
                 + " - Error code: "
                 + jaxbe.getErrorCode()
                 + " - Reason: "
                 + jaxbe.getMessage(), jaxbe.getLinkedException());
      } catch (IOException ioe) {
         throw new ResourceContextException("An I/O error has occured while trying to read resource " + cfgResource.name() + " - Reason: " + ioe.getMessage(), ioe);
      } catch (Exception ex) {
         throw new ResourceContextException("Failed to unmarshall resource " + cfgResource.name() + " - Reason: " + ex.getMessage(), ex);
      }
   }
}
