package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "19_22");
					put("troll2", "19_22");

					// Goblin Aggressor: an [orc] Orc; its bonus needs a possession on it.
					put("aggressor", "12_87");
					// Scurrying Goblin: an [orc] Orc of another title; its play trigger needs an underground site.
					put("scurrying", "11_137");
					// Half-troll of Far Harad: an [orc] minion who is no Orc (a Half-troll); its only text is a
					// start-of-assignment trigger.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: an Orc who is not [orc] ([moria]), with no text.
					put("troop", "1_177");

					// Aragorn, Ranger of the North and Gimli, Son of Gloin: companions who are not the Ring-bearer,
					// each with an ability used only on purpose.
					put("aragorn", "1_89");
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PitTrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Pit Troll
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 13
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Hunter 2</b>. <helper>(While skirmishing a non-hunter character, this character is strength +2.)</helper><br>To play, spot an [orc] Orc.<br>When you play this minion, add 2 threats. The Free Peoples player may exert the Ring-bearer to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troll");

		assertEquals("Pit Troll", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnOrcOrcIsSpottedAndThenAddsTwoThreatsWhichTheFreePeoplesPlayerMayLetStand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		// Three companions: room for three threats.
		scn.MoveCompanionsToTable(aragorn, gimli);

		var troll = scn.GetShadowCard("troll");
		var aggressor = scn.GetShadowCard("aggressor");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(troll);
		assertInZone(Zone.HAND, troll);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		// An [orc] minion who is no Orc, and an Orc who is not [orc].
		scn.MoveMinionsToTable(halftroll, troop);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(troll));

		scn.MoveMinionsToTable(aggressor);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troll));

		assertEquals(3, scn.GetThreatLimit());
		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(troll);

		// No "may" for the Shadow player: nothing is asked of him.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayExertTheRingBearerToPreventTheThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var troll = scn.GetShadowCard("troll");
		var scurrying = scn.GetShadowCard("scurrying");
		scn.MoveCardsToHand(troll);
		assertInZone(Zone.HAND, troll);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		// Another [orc] Orc, alone, is enough to play it.
		scn.MoveMinionsToTable(scurrying);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(troll));
		scn.ShadowPlayCard(troll);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// The Ring-bearer exerts, nobody else does, and no threat is added.  The troll stays.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionPlayedDoesNotSetItOffAndASecondCopyAddsItsThreatsForItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var troll = scn.GetShadowCard("troll");
		var troll2 = scn.GetShadowCard("troll2");
		var scurrying = scn.GetShadowCard("scurrying");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(troll2, scurrying);
		assertInZone(Zone.HAND, troll2, scurrying);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troll, aggressor);
		scn.ShadowRefreshDecision();

		// An [orc] Orc played with a Pit Troll in play: that troll was not the minion played.
		scn.ShadowPlayCard(scurrying);
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(troll2);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		// Two threats, for the copy played; the copy already in play adds none (with it, three would be read).
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheRingBearerExhaustedNothingIsAskedAndTheThreatsAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var troll = scn.GetShadowCard("troll");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(troll);
		assertInZone(Zone.HAND, troll);

		scn.StartGame();
		// Frodo, vitality 4, with 3 wounds: exhausted.  A wound in place of the exertion would kill him.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(troll);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(2, scn.GetThreats());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheThreatLimitThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var troll = scn.GetShadowCard("troll");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(troll);
		assertInZone(Zone.HAND, troll);

		scn.StartGame();
		// Two companions: two threats is the limit.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		scn.ShadowPlayCard(troll);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithRoomForOneThreatThePreventionIsAskedAndOneThreatIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var troll = scn.GetShadowCard("troll");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(troll);
		assertInZone(Zone.HAND, troll);

		scn.StartGame();
		// Two companions and one threat: room for one more.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetThreatLimit());
		scn.ShadowPlayCard(troll);

		// The effect can happen in part, so the prevention is still put.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		// The threats are added one by one (Q-0322) and stop at the limit.
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
