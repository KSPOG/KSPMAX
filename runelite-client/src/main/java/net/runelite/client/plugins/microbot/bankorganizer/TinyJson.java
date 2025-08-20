package net.runelite.client.plugins.microbot.bankorganizer;

final class TinyJson {
    private TinyJson() {}

    static java.util.List<java.util.Map<String,Object>> parseArrayOfObjects(String json) {
        java.util.List<java.util.Map<String,Object>> out = new java.util.ArrayList<>();
        if (json == null) return out;
        String s = json.trim();
        if (!s.startsWith("[") || !s.endsWith("]")) return out;
        s = s.substring(1, s.length()-1).trim();
        java.util.List<String> objs = splitTopObjects(s);
        for (String obj : objs) out.add(parseObject(obj));
        return out;
    }

    static java.util.Map<String,Object> parseObject(String obj) {
        java.util.Map<String,Object> map = new java.util.LinkedHashMap<>();
        if (obj == null) return map;
        String s = obj.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length()-1);
        java.util.List<String> pairs = splitTopPairs(s);
        for (String p : pairs) {
            int idx = p.indexOf(":");
            if (idx < 0) continue;
            String k = unq(p.substring(0, idx).trim());
            String v = p.substring(idx+1).trim();
            if (v.startsWith("[")) map.put(k, parseStringArray(v));
            else map.put(k, unq(v));
        }
        return map;
    }

    private static java.util.List<String> splitTopObjects(String s) {
        java.util.List<String> out = new java.util.ArrayList<>();
        int depth = 0; int start = 0; boolean inStr=false;
        for (int i=0;i<s.length();i++) {
            char c = s.charAt(i);
            if (c=='"') inStr=!inStr;
            else if (!inStr && c=='{') depth++;
            else if (!inStr && c=='}') depth--;
            else if (!inStr && c==',' && depth==0) { out.add(s.substring(start, i)); start = i+1; }
        }
        if (start < s.length()) out.add(s.substring(start));
        return out;
    }

    private static java.util.List<String> splitTopPairs(String s) {
        java.util.List<String> out = new java.util.ArrayList<>();
        int depth = 0; boolean inStr=false; int start=0;
        for (int i=0;i<s.length();i++) {
            char c=s.charAt(i);
            if (c=='"') inStr=!inStr;
            else if (!inStr && c=='[') depth++;
            else if (!inStr && c==']') depth--;
            else if (!inStr && depth==0 && c==',') { out.add(s.substring(start,i)); start=i+1; }
        }
        if (start < s.length()) out.add(s.substring(start));
        return out;
    }

    private static java.util.List<String> parseStringArray(String v) {
        String s = v.trim();
        if (s.startsWith("[")) s=s.substring(1);
        if (s.endsWith("]")) s=s.substring(0,s.length()-1);
        java.util.List<String> out = new java.util.ArrayList<>();
        boolean inStr=false; StringBuilder cur=new StringBuilder();
        for (int i=0;i<s.length();i++) {
            char c=s.charAt(i);
            if (c=='"') { inStr=!inStr; continue; }
            if (c==',' && !inStr) { out.add(unq(cur.toString().trim())); cur.setLength(0); continue; }
            cur.append(c);
        }
        if (cur.length()>0) out.add(unq(cur.toString().trim()));
        return out;
    }

    private static String unq(String x) {
        String t = x.trim();
        if (t.startsWith("\"") && t.endsWith("\"")) t = t.substring(1, t.length()-1);
        t = t.replace("\\\"", "\"").replace("\\\\", "\\");
        return t;
    }

    static String asString(Object o) { return o instanceof String ? (String)o : null; }
    @SuppressWarnings("unchecked") static java.util.List<String> asStringList(Object o) { return (o instanceof java.util.List) ? (java.util.List<String>)o : java.util.Collections.emptyList(); }
}
