package ml.storky.NDCBackuper;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

/**
 * Created by storky on 25.4.16.
 */
public class NetworkDeviceListCell extends ListCell<NetworkDevice> {
    private static final String ROUTER_IMG = "file:resources/router.eps.png";
    private static final String SWITCH_IMG = "file:resources/switch.eps.png";

    @Override
    protected void updateItem(NetworkDevice item, boolean empty) {
        super.updateItem(item, empty);

        if(empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        ImageView imageView = null;

        if(item instanceof Router) {
            imageView = new ImageView(ROUTER_IMG);
            imageView.setFitHeight(15);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

        } else if(item instanceof Switch) {
            imageView = new ImageView(SWITCH_IMG);
            imageView.setFitHeight(12);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

        }

        Label hostname = new Label(item.getHostname());
        Label ip = new Label(item.getIpAddress());

        AnchorPane pane = new AnchorPane(hostname, ip, imageView);
        AnchorPane.setLeftAnchor(imageView, 5d);
        AnchorPane.setTopAnchor(imageView, 5d);

        AnchorPane.setLeftAnchor(hostname, 50d);
        AnchorPane.setLeftAnchor(ip, 50d);
        AnchorPane.setTopAnchor(ip, 12d);
        setGraphic(pane);


    }


}
