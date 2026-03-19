package ee.kaarel.homebudgetappv2.mapper;

import ee.kaarel.homebudgetappv2.dto.NotificationResponse;
import ee.kaarel.homebudgetappv2.model.Notification;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-19T14:43:08+0200",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.1.jar, environment: Java 21.0.7 (Homebrew)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationResponse toResponse(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        NotificationResponse notificationResponse = new NotificationResponse();

        notificationResponse.setId( notification.getId() );
        notificationResponse.setMessage( notification.getMessage() );
        notificationResponse.setRead( notification.isRead() );
        notificationResponse.setCreatedAt( notification.getCreatedAt() );

        return notificationResponse;
    }
}
