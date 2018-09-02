package index.alchemy.api;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public interface IViewSupport {
    
    String getViewName();
    
    void setViewName(String viewName);
    
    default String getDisplayName() {
        return new StringBuilder()
                .append(getClass().getSimpleName())
                .append('<')
                .append(getViewName())
                .append('>')
                .toString();
    }
    
    default String getFullName() {
        if (this instanceof ISubElement && ((ISubElement<?>) this).getParent() instanceof IViewSupport) {
            IViewSupport support = this;
            List<String> viewNames = Lists.newLinkedList();
            do {
                support = (IViewSupport) ((ISubElement<?>) this).getParent();
                viewNames.add(support.getDisplayName());
            } while (this instanceof ISubElement && ((ISubElement<?>) this).getParent() instanceof IViewSupport);
            Collections.reverse(viewNames);
            viewNames.add(support.getDisplayName());
            return Joiner.on('|').join(viewNames);
        }
        else
            return getDisplayName();
    }
    
}
