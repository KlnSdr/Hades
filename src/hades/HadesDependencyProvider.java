package hades;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.files.service.IStaticFileService;
import hades.authorized.HadesAnnotationDiscoverer;
import hades.authorized.service.PermissionService;
import hades.filter.post.ReplaceContextInFilesObserver;
import hades.messaging.service.MessageService;
import hades.security.service.SecurityService;
import hades.update.service.UpdateService;
import hades.user.service.UserService;

@RegisterFor(HadesDependencyProvider.class)
public class HadesDependencyProvider {
    private final PermissionService permissionService;
    private final UpdateService updateService;
    private final UserService userService;
    private final MessageService messageService;
    private final SecurityService securityService;
    private final IStaticFileService staticFileService;
    private final HadesAnnotationDiscoverer hadesAnnotationDiscoverer;
    private final ReplaceContextInFilesObserver replaceContextInFilesObserver;

    @Inject
    public HadesDependencyProvider(PermissionService permissionService, UpdateService updateService, UserService userService, MessageService messageService, SecurityService securityService, IStaticFileService staticFileService, HadesAnnotationDiscoverer hadesAnnotationDiscoverer, ReplaceContextInFilesObserver replaceContextInFilesObserver) {
        this.permissionService = permissionService;
        this.updateService = updateService;
        this.userService = userService;
        this.messageService = messageService;
        this.securityService = securityService;
        this.staticFileService = staticFileService;
        this.hadesAnnotationDiscoverer = hadesAnnotationDiscoverer;
        this.replaceContextInFilesObserver = replaceContextInFilesObserver;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public UpdateService getUpdateService() {
        return updateService;
    }

    public UserService getUserService() {
        return userService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public IStaticFileService getStaticFileService() {
        return staticFileService;
    }

    public HadesAnnotationDiscoverer getHadesAnnotationDiscoverer() {
        return hadesAnnotationDiscoverer;
    }

    public ReplaceContextInFilesObserver getReplaceContextInFilesObserver() {
        return replaceContextInFilesObserver;
    }
}
