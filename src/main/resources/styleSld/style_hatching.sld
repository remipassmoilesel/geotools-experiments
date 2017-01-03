<StyledLayerDescriptor version="1.0.0" schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd">
    <!-- From http://docs.geoserver.org/stable/en/user/styling/sld/cookbook/lines.html#id7 -->
    <NamedLayer>
        <Name>Railroad (hatching)</Name>
        <UserStyle>
            <Title>SLD Cook Book: Railroad (hatching)</Title>
            <FeatureTypeStyle>
                <Rule>
                    <LineSymbolizer>
                        <Stroke>
                            <CssParameter name="stroke">#333333</CssParameter>
                            <CssParameter name="stroke-width">3</CssParameter>
                        </Stroke>
                    </LineSymbolizer>
                    <LineSymbolizer>
                        <Stroke>
                            <GraphicStroke>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://vertline</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke">#333333</CssParameter>
                                            <CssParameter name="stroke-width">1</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>12</Size>
                                </Graphic>
                            </GraphicStroke>
                        </Stroke>
                    </LineSymbolizer>
                </Rule>
            </FeatureTypeStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>