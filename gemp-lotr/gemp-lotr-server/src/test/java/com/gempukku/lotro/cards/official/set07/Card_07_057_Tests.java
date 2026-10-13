package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fatone", "7_57");
					put("gollum", "5_24");     // Gollum, Nasty Treacherous Creature: [gollum] minion, cost 2, site 3; regroup ability used only on purpose
					put("wgollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith] minion, cost 2, site 3; his trigger answers a Nazgul played
					put("shelob", "8_26");     // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum; cost 6, site 8
					put("smeagol", "5_29");    // Smeagol, Slinker: [gollum] companion, cost 0, "To play, add a burden."
					put("ssmeagol", "103_116");// Smeagol, Once Like Us: [shire] companion, cost 1, "To play, add a burden."
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: a companion who is no Smeagol
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FatOneWantsItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Fat One Wants It
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time Gollum or Sméagol is played, add a threat.<br><b>Regroup:</b> Discard this condition to return Gollum to his owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fatone");

		assertEquals("Fat One Wants It", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayingGollumAddsAThreatAndPlayingShelobDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var fatone = scn.GetShadowCard("fatone");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToSupportArea(fatone);
		scn.MoveCardsToHand(gollum, shelob);

		scn.StartGame();
		// Shelob 6 + 2 roaming, Gollum 2 + 2 roaming, at site 2.
		scn.SetTwilight(12);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(shelob);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(gollum);
		assertInPlay(gollum);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingTheWraithGollumAddsAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var fatone = scn.GetShadowCard("fatone");
		var wgollum = scn.GetShadowCard("wgollum");
		scn.MoveCardsToSupportArea(fatone);
		scn.MoveCardsToHand(wgollum);

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(wgollum);
		assertInPlay(wgollum);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingSmeagolAddsAThreatAndGollumPlayedLaterInTheTurnAddsAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(aragorn, smeagol);

		var fatone = scn.GetShadowCard("fatone");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(fatone);
		scn.MoveCardsToHand(gollum);

		scn.StartGame();

		// Another companion played: no threat.
		scn.FreepsPlayCard(aragorn);
		assertEquals(0, scn.GetThreats());

		scn.FreepsPlayCard(smeagol);
		assertInPlay(smeagol);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Three companions leave room for a second threat.
		scn.SetTwilight(4);
		scn.FreepsPass();
		scn.ShadowPlayCard(gollum);
		assertInPlay(gollum);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingTheShireSmeagolAddsAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ssmeagol = scn.GetFreepsCard("ssmeagol");
		scn.MoveCardsToHand(ssmeagol);

		var fatone = scn.GetShadowCard("fatone");
		scn.MoveCardsToSupportArea(fatone);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		scn.FreepsPlayCard(ssmeagol);
		assertInPlay(ssmeagol);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityReturnsGollumToHisOwnersHandAndIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fatone = scn.GetShadowCard("fatone");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToSupportArea(fatone);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fatone));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertInPlay(fatone, gollum, shelob);
		assertTrue(scn.ShadowActionAvailable(fatone));
		scn.ShadowUseCardAction(fatone);

		// One Gollum can be in play, so he is taken unasked; Shelob, a [gollum] minion who is no Gollum, stays.
		assertInDiscard(fatone);
		assertInZone(Zone.HAND, gollum);
		assertTrue(scn.ShadowSeesCardInHand(gollum));
		assertInPlay(shelob);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityReturnsTheWraithGollumToHisOwnersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fatone = scn.GetShadowCard("fatone");
		var wgollum = scn.GetShadowCard("wgollum");
		scn.MoveCardsToSupportArea(fatone);

		scn.StartGame();
		scn.MoveMinionsToTable(wgollum);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertInPlay(fatone, wgollum);
		assertTrue(scn.ShadowActionAvailable(fatone));
		scn.ShadowUseCardAction(fatone);

		assertInDiscard(fatone);
		assertInZone(Zone.HAND, wgollum);
		assertTrue(scn.ShadowSeesCardInHand(wgollum));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoGollumInPlayTheAbilityIsPaidForAndSmeagolStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var fatone = scn.GetShadowCard("fatone");
		scn.MoveCardsToSupportArea(fatone);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertInPlay(fatone);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.ShadowActionAvailable(fatone));
		scn.ShadowUseCardAction(fatone);

		assertInDiscard(fatone);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
