/* Copyright (c) 2001-2010, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.jdbc.pool;

import java.util.Arrays;

import javax.transaction.xa.Xid;

/**
 * Implementation of Xid for tests.
 *
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 2.0.1
 */
public class JDBCXID implements Xid {

    int    formatID;
    byte[] txID;
    byte[] txBranch;

    public int getFormatId() {
        return formatID;
    }

    public byte[] getGlobalTransactionId() {
        return txID;
    }

    public byte[] getBranchQualifier() {
        return txBranch;
    }

    public JDBCXID(int formatID, byte[] txID, byte[] txBranch) {

        this.formatID = formatID;
        this.txID     = txID;
        this.txBranch = txBranch;
    }

    public int hashCode() {
        return formatID;
    }

    public boolean equals(Object other) {

        if (other instanceof Xid) {
            Xid o = (Xid) other;

            return formatID == o.getFormatId()
                   && Arrays.equals(txID, o.getGlobalTransactionId())
                   && Arrays.equals(txBranch, o.getBranchQualifier());
        }

        return false;
    }
}