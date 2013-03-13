/* Copyright 2007-2011 MTA SZTAKI LPDS, Budapest

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */
/*
 * Proxy Provider service
 */

package hu.sztaki.lpds.pgportal.services.credential;

import hu.sztaki.lpds.pgportal.services.asm.ASMCredentialProvider;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * @author krisztian karoczkai
 */
@WebService()
public class CredentialProvider {

    /**
     * Web service operation. Getting proxy as a byte array
     * @param pGroup user group
     * @param pUser user id
     * @param pMiddleware middleware name
     * @param pVo grid/vo/group name
     * @return user proxy
     * @throws Proxy does not exsists, file i/o error
     */
    @WebMethod(operationName = "get")
    public byte[] get(@WebParam(name = "pGroup")
    String pGroup, @WebParam(name = "pUser")
    String pUser, @WebParam(name = "pMiddleware")
    String pMiddleware, @WebParam(name = "pVo")
    String pVo) throws Exception {
        ASMCredentialProvider asmcred = new ASMCredentialProvider();
        return asmcred.get(pUser, pVo);
       
    }

}
