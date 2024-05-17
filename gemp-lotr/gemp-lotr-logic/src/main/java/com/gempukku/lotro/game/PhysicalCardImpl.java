package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ModifierHook;

import java.util.LinkedList;
import java.util.List;

public class PhysicalCardImpl implements PhysicalCard {
    private int _cardId;
    private final String _blueprintId;
    private final String _owner;
    private String _cardController;
    private Zone _zone;
    private final LotroCardBlueprint _blueprint;

    private PhysicalCardImpl _attachedTo;
    private PhysicalCardImpl _stackedOn;

    private List<ModifierHook> _modifierHooks;
    private List<ModifierHook> _modifierHooksStacked;
    private List<ModifierHook> _modifierHooksInDiscard;
    private List<ModifierHook> _modifierHooksControlledSite;
    private List<ModifierHook> _modifierHooksPermanentSite;

    private WhileInZoneData _whileInZoneData;

    private Integer _siteNumber;

    public PhysicalCardImpl(int cardId, String blueprintId, String owner, LotroCardBlueprint blueprint) {
        _cardId = cardId;
        _blueprintId = blueprintId;
        _owner = owner;
        _blueprint = blueprint;
    }

    public void setCardId(int cardId) {
        _cardId = cardId;
    }

    @Override
    public String getBlueprintId() {
        return _blueprintId;
    }

    public void setZone(Zone zone) {
        _zone = zone;
    }

    @Override
    public Zone getZone() {
        return _zone;
    }

    @Override
    public String getOwner() {
        return _owner;
    }

    public void setCardController(String siteController) {
        _cardController = siteController;
    }

    @Override
    public String getCardController() {
        return _cardController;
    }

    public void startAffectingGame(LotroGame game) {
        List<? extends Modifier> modifiers = _blueprint.getInPlayModifiers(game, this);
        if (modifiers != null) {
            _modifierHooks = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooks.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGame() {
        if (_modifierHooks != null) {
            for (ModifierHook modifierHook : _modifierHooks)
                modifierHook.stop();
            _modifierHooks = null;
        }
    }

    public void startAffectingGameStacked(LotroGame game) {
        List<? extends Modifier> modifiers = _blueprint.getStackedOnModifiers(game, this);
        if (modifiers != null) {
            _modifierHooksStacked = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooksStacked.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGameStacked() {
        if (_modifierHooksStacked != null) {
            for (ModifierHook modifierHook : _modifierHooksStacked)
                modifierHook.stop();
            _modifierHooksStacked = null;
        }
    }

    public void startAffectingGameInDiscard(LotroGame game) {
        List<? extends Modifier> modifiers = _blueprint.getInDiscardModifiers(game, this);
        if (modifiers != null) {
            _modifierHooksInDiscard = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooksInDiscard.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGameInDiscard() {
        if (_modifierHooksInDiscard != null) {
            for (ModifierHook modifierHook : _modifierHooksInDiscard)
                modifierHook.stop();
            _modifierHooksInDiscard = null;
        }
    }

    public void startAffectingGamePermanentSite(LotroGame game) {
        List<? extends Modifier> modifiers = _blueprint.getPermanentSiteModifiers(game, this);
        if (modifiers != null) {
            _modifierHooksPermanentSite = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooksPermanentSite.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGameControlledSite() {
        if (_modifierHooksControlledSite != null) {
            for (ModifierHook modifierHook : _modifierHooksControlledSite)
                modifierHook.stop();
            _modifierHooksControlledSite = null;
        }
    }

    public void startAffectingGameControlledSite(LotroGame game) {
        List<? extends Modifier> modifiers = _blueprint.getControlledSiteModifiers(game, this);
        if (modifiers != null) {
            _modifierHooksControlledSite = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooksControlledSite.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    @Override
    public int getCardId() {
        return _cardId;
    }

    @Override
    public LotroCardBlueprint getBlueprint() {
        return _blueprint;
    }

    public void attachTo(PhysicalCardImpl physicalCard) {
        _attachedTo = physicalCard;
    }

    @Override
    public PhysicalCard getAttachedTo() {
        return _attachedTo;
    }

    public void stackOn(PhysicalCardImpl physicalCard) {
        _stackedOn = physicalCard;
    }

    @Override
    public PhysicalCard getStackedOn() {
        return _stackedOn;
    }

    @Override
    public WhileInZoneData getWhileInZoneData() {
        return _whileInZoneData;
    }

    @Override
    public void setWhileInZoneData(WhileInZoneData whileInZoneData) {
        _whileInZoneData = whileInZoneData;
    }

    @Override
    public Integer getSiteNumber() {
        return _siteNumber;
    }

    @Override
    public void setSiteNumber(Integer number) {
        _siteNumber = number;
    }
}
