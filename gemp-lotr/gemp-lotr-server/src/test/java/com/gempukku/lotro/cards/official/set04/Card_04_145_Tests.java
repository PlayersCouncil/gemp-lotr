package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_145_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cloud", "4_145");

					put("assassin", "1_131");   // Orthanc Assassin: unique [isengard] Uruk-hai archer, vitality 3; damage +1 and nothing else
					put("crossbow", "4_182");   // Uruk Crossbow Troop: [isengard] Uruk-hai archer, vitality 2; its when-played token does nothing when cheated in
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, no archer; damage +1 and nothing else
					// Lurtz, Halfling Hunter: unique [uruk-hai] Uruk-hai archer, vitality 3 (the race and keyword without the
					// culture); a maneuver and an assignment ability used only on purpose
					put("lurtz", "18_118");

					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: Elf companion and archer; stronger only against a Nazgul
					put("garrison", "8_36");    // Garrison of Gondor: [gondor] Man, archer; defender +1 only with a fortification (none here)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CloudofArrowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Cloud of Arrows
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: <b>Archery:</b> Exert an [isengard] archer to make the minion archery total +1 and the fellowship archery total -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cloud");

		assertEquals("Cloud of Arrows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingAnIsengardArcherMakesTheMinionTotalOneMoreAndTheFellowshipTotalOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloud = scn.GetShadowCard("cloud");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var savage = scn.GetShadowCard("savage");
		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCardsToHand(cloud);

		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(legolas, garrison);

		scn.StartGame();
		assertInHand(cloud);
		scn.MoveMinionsToTable(assassin, crossbow, savage, lurtz);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Three archer minions (the Assassin, the Crossbow Troop, Lurtz) and two archer companions.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertInHand(cloud);
		assertTrue(scn.ShadowPlayAvailable(cloud));
		scn.ShadowPlayCard(cloud);

		// [isengard] archers of two titles; not the [isengard] Uruk-hai who is no archer, nor the [uruk-hai] archer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(assassin, crossbow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, lurtz));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(assassin);

		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(crossbow));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertEquals(4, scn.GetShadowArcheryTotal());   // 3 + 1
		assertEquals(1, scn.GetFreepsArcheryTotal());   // 2 - 1
		assertInZone(Zone.DISCARD, cloud);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyIsengardArcherIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloud = scn.GetShadowCard("cloud");
		var crossbow = scn.GetShadowCard("crossbow");
		var savage = scn.GetShadowCard("savage");
		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCardsToHand(cloud);

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		assertInHand(cloud);
		scn.MoveMinionsToTable(crossbow, savage, lurtz);
		// The Crossbow Troop, vitality 2, with 1 wound: exhausted.  The Savage and Lurtz beside it can exert.
		scn.AddWoundsToChar(crossbow, 1);
		assertEquals(1, scn.GetWoundsOn(crossbow));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(lurtz));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(cloud));
		assertInHand(cloud);
	}

	@Test
	public void TheTotalsHoldThroughArcheryFireAndAreBackInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var cloud = scn.GetShadowCard("cloud");
		var crossbow = scn.GetShadowCard("crossbow");
		scn.MoveCardsToHand(cloud);

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);

		scn.StartGame();
		assertInHand(cloud);
		scn.MoveMinionsToTable(crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		scn.ShadowPlayCard(cloud);
		// The one [isengard] archer is chosen unasked, and exerts.
		assertEquals(1, scn.GetWoundsOn(crossbow));
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// Still in force as archery fire is resolved: two wounds for the Free Peoples player to place, and none on the
		// Crossbow Troop from the fellowship.
		scn.PassArcheryActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(crossbow));

		// Gone with the archery phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
	}
}
