package cloud.waldiekiste.java.projekte.cloudnet.webinterface.http.v2;

import cloud.waldiekiste.java.projekte.cloudnet.webinterface.ProjectMain;
import cloud.waldiekiste.java.projekte.cloudnet.webinterface.http.v2.utils.RequestUtil;
import cloud.waldiekiste.java.projekte.cloudnet.webinterface.http.v2.utils.ResponseUtil;
import cloud.waldiekiste.java.projekte.cloudnet.webinterface.http.v2.utils.UserUtil;
import de.dytanic.cloudnet.lib.utility.document.Document;
import de.dytanic.cloudnet.web.server.handler.MethodWebHandlerAdapter;
import de.dytanic.cloudnet.web.server.util.PathProvider;
import de.dytanic.cloudnet.web.server.util.QueryDecoder;
import de.dytanic.cloudnetcore.CloudNet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.atomic.AtomicInteger;

public class DashboardAPI extends MethodWebHandlerAdapter {
    private final ProjectMain projectMain;

    public DashboardAPI(CloudNet cloudNet, ProjectMain projectMain) {
        super("/cloudnet/api/v2/dashboard");
        cloudNet.getWebServer().getWebServerProvider().registerHandler(this);
        this.projectMain = projectMain;
    }
    @SuppressWarnings( "deprecation" )
    @Override
    public FullHttpResponse get(ChannelHandlerContext channelHandlerContext, QueryDecoder queryDecoder, PathProvider pathProvider, HttpRequest httpRequest) {
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(httpRequest.getProtocolVersion(), HttpResponseStatus.OK);
        ResponseUtil.setHeader(fullHttpResponse, "Content-Type", "application/json; charset=utf-8");
        if (!RequestUtil.hasHeader(httpRequest, "-xcloudnet-user", "-xcloudnet-passwort", "-xcloudnet-message")) {
            return ResponseUtil.xCloudFieldsNotFound(fullHttpResponse);
        }
        String username = RequestUtil.getHeaderValue(httpRequest, "-xcloudnet-user");
        String userpassword = RequestUtil.getHeaderValue(httpRequest, "-xcloudnet-password");
        if (!CloudNet.getInstance().authorizationPassword(username, userpassword)) {
            return UserUtil.failedAuthorization(fullHttpResponse);
        }
        //User user = CloudNet.getInstance().getUser(username);
        switch (RequestUtil.getHeaderValue(httpRequest, "-Xmessage").toLowerCase()) {
            case "players":{
                Document document = new Document();
                AtomicInteger integer = new AtomicInteger();
                getProjectMain().getCloud().getServerGroups().keySet().forEach(t-> integer.getAndAdd(getProjectMain().getCloud().getOnlineCount(t)));
                document.append("response",integer.get());
                return ResponseUtil.success(fullHttpResponse,true,document);
            }
            case "servers":{
                Document document = new Document();
                document.append("response",getProjectMain().getCloud().getServers().size());
                return ResponseUtil.success(fullHttpResponse,true,document);
            }
            case "proxys":{
                Document document = new Document();
                document.append("response",getProjectMain().getCloud().getProxys().size());
                return ResponseUtil.success(fullHttpResponse,true,document);
            }
            case "groups":{
                Document document = new Document();
                document.append("response",getProjectMain().getCloud().getServerGroups().size());
                return ResponseUtil.success(fullHttpResponse,true,document);
            }
            default:{
                return ResponseUtil.xMessageFieldNotFound(fullHttpResponse);
            }
        }
    }
    @SuppressWarnings( "deprecation" )
    @Override
    public FullHttpResponse options(ChannelHandlerContext channelHandlerContext, QueryDecoder queryDecoder, PathProvider pathProvider, HttpRequest httpRequest) {
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(httpRequest.getProtocolVersion(), HttpResponseStatus.OK);
        fullHttpResponse.headers().set("Content-Type", "application/json");
        fullHttpResponse.headers().set("Access-Control-Allow-Credentials", "true");
        fullHttpResponse.headers().set("Access-Control-Allow-Headers", "content-type, if-none-match, -Xcloudnet-token, -Xmessage, -Xvalue, -Xcloudnet-user, -Xcloudnet-password,-Xcount");
        fullHttpResponse.headers().set("Access-Control-Allow-Methods", "POST,GET,OPTIONS");
        fullHttpResponse.headers().set("Access-Control-Allow-Origin", "*");
        fullHttpResponse.headers().set("Access-Control-Max-Age", "3600");
        return fullHttpResponse;
    }

    private ProjectMain getProjectMain() {
        return projectMain;
    }
}
