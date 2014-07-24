/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.overlord.commons.auth.util;

import static org.picketlink.common.util.StringUtil.isNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.util.Base64;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;

/**
 * Utility for the HTTP/Post binding
 *
 * @author Anil.Saldhana@redhat.com
 * @since May 22, 2009
 */
public class SamlPostBindingUtil {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * Apply base64 encoding on the message
     *
     * @param stringToEncode
     *
     * @return
     */
    public static String base64Encode(String stringToEncode) throws IOException {
        return Base64.encodeBytes(stringToEncode.getBytes("UTF-8"), Base64.DONT_BREAK_LINES); //$NON-NLS-1$
    }

    /**
     * Apply base64 decoding on the message and return the byte array
     *
     * @param encodedString
     *
     * @return
     */
    public static byte[] base64Decode(String encodedString) {
        if (encodedString == null)
            throw logger.nullArgumentError("encodedString"); //$NON-NLS-1$

        return Base64.decode(encodedString);
    }

    /**
     * Apply base64 decoding on the message and return the stream
     *
     * @param encodedString
     *
     * @return
     */
    public static InputStream base64DecodeAsStream(String encodedString) {
        if (encodedString == null)
            throw logger.nullArgumentError("encodedString"); //$NON-NLS-1$

        return new ByteArrayInputStream(base64Decode(encodedString));
    }

    /**
     * Send the response to the redirected destination while adding the character encoding of "UTF-8" as well as adding
     * headers
     * for cache-control and Pragma
     *
     * @param destination Destination URI where the response needs to redirect
     * @param response HttpServletResponse
     *
     * @throws IOException
     */
    public static void sendPost(DestinationInfoHolder holder, HttpServletResponse response, boolean request) throws IOException {
        String key = request ? GeneralConstants.SAML_REQUEST_KEY : GeneralConstants.SAML_RESPONSE_KEY;

        String relayState = holder.getRelayState();
        String destination = holder.getDestination();
        String samlMessage = holder.getSamlMessage();

        if (destination == null) {
            throw logger.nullValueError("Destination is null"); //$NON-NLS-1$
        }

        response.setContentType("text/html"); //$NON-NLS-1$
        common(holder.getDestination(), response);
        StringBuilder builder = new StringBuilder();

        builder.append("<HTML>"); //$NON-NLS-1$
        builder.append("<HEAD>"); //$NON-NLS-1$

        if (request)
            builder.append("<TITLE>HTTP Post Binding (Request)</TITLE>"); //$NON-NLS-1$
        else
            builder.append("<TITLE>HTTP Post Binding Response (Response)</TITLE>"); //$NON-NLS-1$

        builder.append("</HEAD>"); //$NON-NLS-1$
        builder.append("<BODY Onload=\"document.forms[0].submit()\">"); //$NON-NLS-1$

        builder.append("<FORM METHOD=\"POST\" ACTION=\"" + destination + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + key + "\"" + " VALUE=\"" + samlMessage + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        if (isNotNull(relayState)) {
            builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"RelayState\" " + "VALUE=\"" + relayState + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        builder.append("<NOSCRIPT>"); //$NON-NLS-1$
        builder.append("<P>JavaScript is disabled. We strongly recommend to enable it. Click the button below to continue.</P>"); //$NON-NLS-1$
        builder.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"CONTINUE\" />"); //$NON-NLS-1$
        builder.append("</NOSCRIPT>"); //$NON-NLS-1$

        builder.append("</FORM></BODY></HTML>"); //$NON-NLS-1$

        String str = builder.toString();

        logger.trace(str);

        ServletOutputStream outputStream = response.getOutputStream();

        // we need to re-configure the content length, because Tomcat may have written some content.
        response.resetBuffer();
        response.setContentLength(str.length() + 1);

        outputStream.println(str);
        outputStream.close();
    }

    private static void common(String destination, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        response.setHeader("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
        response.setHeader("Cache-Control", "no-cache, no-store"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}