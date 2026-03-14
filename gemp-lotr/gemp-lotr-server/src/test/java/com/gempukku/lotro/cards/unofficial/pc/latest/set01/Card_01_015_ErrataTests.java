package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_015_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helm", "51_15");
					put("gimli", "1_12"); // Gimli, Dwarf of Erebor (STR 6, VIT 3)
					put("guard", "1_7");  // Dwarf Guard (STR 4, VIT 2, companion)
					put("lurtz", "1_127"); // Lurtz, Servant of Isengard (STR 13, VIT 3, Damage+1, Archer)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimlisHelmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gimli's Helm
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Helm
		 * Game Text: Bearer must be Gimli.<br>He takes no more than 1 wound during each skirmish phase.
		 * <br><b>Skirmish:</b> Discard Gimli's Helm to make Gimli unable to take wounds.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("helm");

		assertEquals("Gimli's Helm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HELM));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GimlisHelmPassiveLimitsWoundsToOneDuringSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("gimli");
		var lurtz = scn.GetShadowCard("lurtz");

		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, helm);
		scn.MoveMinionsToTable(lurtz);

		scn.StartGame();

		// Gimli (STR 6) vs Lurtz (STR 13, Damage+1).
		// Without the helm, Gimli would be overwhelmed (13 >= 2*6) and die.
		// With the helm's passive, Gimli takes no more than 1 wound during the skirmish phase.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, lurtz);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
	}

	@Test
	public void GimlisHelmDiscardAbilityMakesGimliUnableToTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("gimli");
		var lurtz = scn.GetShadowCard("lurtz");

		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, helm);
		scn.MoveMinionsToTable(lurtz);

		scn.StartGame();

		// Gimli (STR 6) vs Lurtz (STR 13, Damage+1).
		// During skirmish, discard the helm to make Gimli unable to take wounds.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, lurtz);

		// Use the helm's activated ability
		assertTrue(scn.FreepsActionAvailable(helm));
		scn.FreepsUseCardAction(helm);
		assertInDiscard(helm);

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		// Gimli takes 0 wounds despite losing/being overwhelmed
		assertEquals(0, scn.GetWoundsOn(gimli));
	}

	@Test
	public void GimlisHelmDiscardPreventsGimliFromBeingTargetedByThreatWounds() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var lurtz = scn.GetShadowCard("lurtz");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(gimli, helm);
		scn.MoveMinionsToTable(lurtz);

		scn.StartGame();

		scn.AddThreats(1);

		// Assign Guard (STR 4) vs Lurtz (STR 13) -- Guard will be overwhelmed and die.
		// Gimli is NOT assigned to any skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, lurtz);
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		// During skirmish, use helm to make Gimli unable to take wounds
		assertTrue(scn.FreepsActionAvailable(helm));
		scn.FreepsUseCardAction(helm);
		assertInDiscard(helm);

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		// Guard is overwhelmed by Lurtz and dies.
		// 1 threat fires -- Gimli can't take wounds, so Frodo is the only valid target.
		// With only 1 valid target, Frodo is auto-selected for the threat wound.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetThreats());
	}
}
