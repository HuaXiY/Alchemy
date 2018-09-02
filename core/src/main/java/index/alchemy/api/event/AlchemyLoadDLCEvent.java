package index.alchemy.api.event;

import java.io.File;

import index.alchemy.api.IDLCInfo;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class AlchemyLoadDLCEvent extends Event {

    public final IDLCInfo dlc;
    public final File file;

    public AlchemyLoadDLCEvent(IDLCInfo dlc, File file) {
        this.dlc = dlc;
        this.file = file;
    }

    @Cancelable
    public static class Pre extends AlchemyLoadDLCEvent {

        public Pre(IDLCInfo dlc, File file) {
            super(dlc, file);
        }

    }

    public static class Post extends AlchemyLoadDLCEvent {

        public Post(IDLCInfo dlc, File file) {
            super(dlc, file);
        }

    }

}