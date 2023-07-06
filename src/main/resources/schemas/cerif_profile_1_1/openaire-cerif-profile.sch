<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
            xmlns:xs="http://www.w3.org/2001/XMLSchema"
            queryBinding="xslt2">
   <sch:ns xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
           xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc"
           xmlns="https://www.openaire.eu/cerif-profile/1.1/"
           xmlns:cf="urn:xmlns:org.eurocris.cerif"
           xmlns:cflink="https://w3id.org/cerif/annotations#"
           
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           prefix="oacf"
           uri="https://www.openaire.eu/cerif-profile/1.1/"/>
   <sch:ns xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
           xmlns:skos="http://www.w3.org/2004/02/skos/core#"
           xmlns:skosxl="http://www.w3.org/2008/05/skos-xl#"
           xmlns:cf="urn:xmlns:org.eurocris.cerif"
           xmlns:dc-term="http://purl.org/dc/terms/"
           xmlns="http://purl.org/coar/access_right"
           prefix="coar-access-right"
           uri="http://purl.org/coar/access_right"/>
   <sch:pattern xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
                xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc"
                xmlns="https://www.openaire.eu/cerif-profile/1.1/"
                xmlns:cf="urn:xmlns:org.eurocris.cerif"
                xmlns:cflink="https://w3id.org/cerif/annotations#"
                
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <sch:title>Occurrence of "uri" implies an OAMandate is mandated</sch:title>
      <sch:rule context="oacf:OAMandate">
         <sch:report test="@uri and not ( @mandated = 'true' )">If the URI of an Open Access policy is given, "mandated" must be set true</sch:report>
              </sch:rule>
           </sch:pattern>
   <sch:pattern xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:skosxl="http://www.w3.org/2008/05/skos-xl#"
                xmlns:cf="urn:xmlns:org.eurocris.cerif"
                xmlns:dc-term="http://purl.org/dc/terms/"
                xmlns="http://purl.org/coar/access_right">
                  <sch:title>Occurrence of "startDate" and "endDate" with the COAR Access Rights vocabulary</sch:title>
                  <sch:rule context="coar-access-right:Access">
                     <sch:report test="@startDate">No "startDate" shall be specified for an access item</sch:report>
                     <sch:assert test="@endDate or not ( string(.) = 'http://purl.org/coar/access_right/c_f1cf' )">An "endDate" may only be specified for an embargoed access item</sch:assert>
                     <sch:assert test="not( @endDate ) or ( string(.) = 'http://purl.org/coar/access_right/c_f1cf' )">No "endDate" may be specified for an item with other than embargoed access</sch:assert>
                  </sch:rule>
               </sch:pattern>
   <sch:pattern xmlns:cf="urn:xmlns:org.eurocris.cerif"
                xmlns:cflink="https://w3id.org/cerif/annotations#"
                
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <sch:title>"startDate" precedes the corresponding "endDate"</sch:title>
                <sch:rule context="*[@startDate][@endDate][ string-length( @startDate ) le 10 and not( contains( @startDate, 'Z' ) or contains( @startDate, ':' ) ) ][ string-length( @endDate ) = 4 and not( contains( @endDate, 'Z' ) or contains( @endDate, ':' ) ) ]">
                    <sch:report test="( substring( concat( @startDate, '-01-01' ), 1, 10 ) cast as xs:date ) gt ( ( concat( @endDate, '-01-01' ) cast as xs:date ) + xs:yearMonthDuration( 'P1Y' ) )">The "startDate" (<sch:value-of select="@startDate"/>) must not be later than the end of the corresponding "endDate" (<sch:value-of select="@endDate"/>) period</sch:report>
                </sch:rule>
                <sch:rule context="*[@startDate][@endDate][ string-length( @startDate ) le 10 and not( contains( @startDate, 'Z' ) or contains( @startDate, ':' ) ) ][ string-length( @endDate ) = 7 and not( contains( @endDate, 'Z' ) or contains( @endDate, ':' ) ) ]">
                    <sch:report test="( substring( concat( @startDate, '-01-01' ), 1, 10 ) cast as xs:date ) gt ( ( concat( @endDate, '-01' ) cast as xs:date ) + xs:yearMonthDuration( 'P1M' ) )">The "startDate" (<sch:value-of select="@startDate"/>) must not be later than the end of the corresponding "endDate" (<sch:value-of select="@endDate"/>) period</sch:report>
                </sch:rule>
                <sch:rule context="*[@startDate][@endDate][ string-length( @startDate ) le 10 and not( contains( @startDate, 'Z' ) or contains( @startDate, ':' ) ) ][ string-length( @endDate ) = 10 and not( contains( @endDate, 'Z' ) or contains( @endDate, ':' ) ) ]">
                    <sch:report test="( substring( concat( @startDate, '-01-01' ), 1, 10 ) cast as xs:date ) gt ( ( @endDate cast as xs:date ) + xs:dayTimeDuration( 'P1D' ) )">The "startDate" (<sch:value-of select="@startDate"/>) must not be later than the end of the corresponding "endDate" (<sch:value-of select="@endDate"/>) period</sch:report>
                </sch:rule>
            </sch:pattern>
   <sch:diagnostics/>
</sch:schema>
