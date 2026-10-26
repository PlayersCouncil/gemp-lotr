package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("berserker", "8_96");
					put("berserker2", "8_96");

					put("guard", "1_7");        // Dwarf Guard: a companion with no text once in play, so that two threats can lie
					put("hand1", "1_7");        // Dwarf Guards to make up the Free Peoples hand, for initiative
					put("hand2", "1_7");
					put("hand3", "1_7");
					put("hand4", "1_7");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc minion who is no besieger; his text answers only a weapon played on him
					put("pillager", "7_275");   // Gorgoroth Pillager: [sauron] Orc besieger; in play its text does nothing
					put("troll", "8_102");      // Great Hill Troll: [sauron] Troll besieger; its play trigger is not reached
					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothBerserkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gorgoroth Berserker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>. <b>Enduring</b>. To play, spot a [sauron] Orc.<br><b>Maneuver:</b> If you have initiative, exert this minion twice to take control of a site.<br><b>Maneuver:</b> Remove a threat and exert this minion twice to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("berserker");

		assertEquals("Gorgoroth Berserker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilASauronOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var chieftain = scn.GetShadowCard("chieftain");
		var troll = scn.GetShadowCard("troll");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCardsToHand(berserker);
		assertInHand(berserker);

		scn.StartGame();
		// A [sauron] minion that is no Orc, and an Orc that is not [sauron].
		scn.MoveMinionsToTable(troll, patrol);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, troll, patrol);
		assertFalse(scn.ShadowPlayAvailable(berserker));

		scn.MoveMinionsToTable(chieftain);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(berserker));

		scn.ShadowPlayCard(berserker);
		assertInZone(Zone.SHADOW_CHARACTERS, berserker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABesiegerOrcIsAlsoASauronOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var pillager = scn.GetShadowCard("pillager");
		scn.MoveCardsToHand(berserker);
		assertInHand(berserker);

		scn.StartGame();
		scn.MoveMinionsToTable(pillager);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertTrue(scn.ShadowPlayAvailable(berserker));
		scn.ShadowPlayCard(berserker);
		assertInZone(Zone.SHADOW_CHARACTERS, berserker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithInitiativeItExertsTwiceToTakeControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var berserker2 = scn.GetShadowCard("berserker2");
		scn.MoveCompanionsToTable("guard");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(berserker, berserker2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetFreepsHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		// Four cards in the Free Peoples hand: he has initiative, and there is no threat to remove.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(berserker));

		scn.SetFreepsHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetWoundsOn(berserker2));
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.ShadowActionAvailable(berserker, "Exert this minion twice"));
		assertFalse(scn.ShadowActionAvailable(berserker, "Remove a threat"));

		scn.ShadowUseCardAction(berserker, "Exert this minion twice");
		assertEquals(2, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetWoundsOn(berserker2));
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithoutInitiativeItRemovesAThreatAndExertsTwiceToTakeControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var berserker2 = scn.GetShadowCard("berserker2");
		scn.MoveCompanionsToTable("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(berserker, berserker2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetFreepsHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(berserker));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetWoundsOn(berserker2));
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.ShadowActionAvailable(berserker, "Remove a threat"));

		scn.ShadowUseCardAction(berserker, "Remove a threat");
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetWoundsOn(berserker2));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACopyThatCannotExertTwiceIsOfferedNeitherAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var berserker2 = scn.GetShadowCard("berserker2");
		scn.MoveCompanionsToTable("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(berserker, berserker2);
		// Vitality 3 with 1 wound: 2 left, one short of exerting twice.
		scn.AddWoundsToChar(berserker, 1);
		scn.AddThreats(1);
		assertEquals(1, scn.GetWoundsOn(berserker));
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetFreepsHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));

		// The Shadow has initiative and there is a threat to remove: the unwounded copy is offered both.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(berserker));
		assertTrue(scn.ShadowActionAvailable(berserker2, "Remove a threat"));
		assertTrue(scn.ShadowActionAvailable(berserker2, "Exert this minion twice"));
	}

	@Test
	public void NeitherAbilityIsOfferedInTheShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		scn.MoveCompanionsToTable("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(berserker);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();
		scn.SetFreepsHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));

		// Initiative, a threat and a site to take, but no maneuver phase.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.ShadowActionAvailable(berserker));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetFreepsHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(berserker, "Remove a threat"));
		assertTrue(scn.ShadowActionAvailable(berserker, "Exert this minion twice"));
	}
}
