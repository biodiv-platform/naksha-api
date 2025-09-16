package com.strandls.naksha.pojo;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "layer_portal_mapping")
@IdClass(LayerPortalMapping.LayerPortalMappingId.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LayerPortalMapping implements Serializable {
    private static final long serialVersionUID = -7235401478350962938L;

    @Id
    @Column(name = "layer_id")
    private Long layerId;

    @Id
    @Column(name = "portal_id")
    private Long portalId;

    public LayerPortalMapping() {}

    public LayerPortalMapping(Long layerId, Long portalId) {
        this.layerId = layerId;
        this.portalId = portalId;
    }

    public Long getLayerId() { return layerId; }
    public void setLayerId(Long layerId) { this.layerId = layerId; }

    public Long getPortalId() { return portalId; }
    public void setPortalId(Long portalId) { this.portalId = portalId; }

    // Composite key class for @IdClass
    public static class LayerPortalMappingId implements Serializable {
        private Long layerId;
        private Long portalId;

        public LayerPortalMappingId() {}

        public LayerPortalMappingId(Long layerId, Long portalId) {
            this.layerId = layerId;
            this.portalId = portalId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LayerPortalMappingId)) return false;
            LayerPortalMappingId that = (LayerPortalMappingId) o;
            return Objects.equals(layerId, that.layerId)
                && Objects.equals(portalId, that.portalId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(layerId, portalId);
        }
    }
}
