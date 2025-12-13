package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("twofaced", "103_15");
					put("pass", "103_12"); // Gollum card to hinder/restore
					put("gollum1", "9_28"); // Gollum, Dark as Darkness
					put("gollum2", "5_24"); // Gollum, Nasty Treacherous Creature
					put("gollummad", "10_21"); // Gollum, Mad Thing - has assignment trigger
					put("shelob1", "8_26"); // Shelob, Last Child of Ungoliant
					put("shelob2", "10_23"); // Shelob, Her Ladyship
					put("letdeal", "7_63"); // Let Her Deal With Them - attaches to minion
					put("runner", "1_178");

					put("aragorn", "1_89");
					put("glorfindel", "9_16");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TwofacedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Two-faced
		 * Unique: false
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Shadow</b> <i>or</i> <b>Maneuver</b> <i>or</i> <b>Skirmish</b>: Restore a [gollum] card.  Then you may spot a [gollum] minion and remove a threat to replace it with another minion from your discard pile with the same title (keep all tokens and borne cards).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("twofaced");

		assertEquals("Two-faced", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void TwofacedRestoresGollumCardInShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.HinderCard(pass);
		assertTrue(scn.IsHindered(pass));
		scn.AddThreats(1);

		assertTrue(scn.ShadowPlayAvailable(twofaced));
		scn.ShadowPlayCard(twofaced);

		// Pass is chosen automatically to restore as the only option
		assertFalse(scn.IsHindered(pass));

		// no Gollum minion in play
		assertInZone(Zone.DISCARD, twofaced);
	}

	@Test
	public void TwofacedWorksInManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.HinderCard(pass);
		scn.AddThreats(1);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(twofaced));
		scn.ShadowPlayCard(twofaced);

		assertFalse(scn.IsHindered(pass));
	}

	@Test
	public void TwofacedWorksInSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(runner);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		scn.HinderCard(pass);
		scn.AddThreats(1);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(twofaced));
		scn.ShadowPlayCard(twofaced);

		assertFalse(scn.IsHindered(pass));
	}

	@Test
	public void TwofacedCanPlayWithNoHinderedGollumCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(runner);
		// pass is NOT hindered

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.AddThreats(1);

		// Can still play - restore effect fizzles but event is playable
		assertTrue(scn.ShadowPlayAvailable(twofaced));
		scn.ShadowPlayCard(twofaced);

		// No hindered cards to restore, effect fizzles
		assertFalse(scn.IsHindered(pass));
		assertInZone(Zone.DISCARD, twofaced);
	}

	@Test
	public void TwofacedReplacementNotOfferedWithoutThreats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var gollum1 = scn.GetShadowCard("gollum1");
		var gollum2 = scn.GetShadowCard("gollum2");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(gollum1);
		scn.MoveCardsToDiscard(gollum2);
		// No threats

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.HinderCard(pass);
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(twofaced);
		scn.ShadowChooseCard(pass);

		// Optional replacement NOT offered - no threats to remove
		assertInZone(Zone.SHADOW_CHARACTERS, gollum1);
		assertInZone(Zone.DISCARD, twofaced);
	}

	@Test
	public void TwofacedReplacementNotOfferedWithoutValidDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var gollum1 = scn.GetShadowCard("gollum1");
		// No other Gollum with different subtitle in discard

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(gollum1);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.HinderCard(pass);
		scn.AddThreats(1);

		scn.ShadowPlayCard(twofaced);
		scn.ShadowChooseCard(pass);

		// Optional replacement NOT offered - no valid discard target
		assertInZone(Zone.DISCARD, twofaced);
	}

	@Test
	public void TwofacedReplacesGollumWithDifferentSubtitle() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var gollum1 = scn.GetShadowCard("gollum1");
		var gollum2 = scn.GetShadowCard("gollum2");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(gollum1, runner);
		scn.MoveCardsToDiscard(gollum2);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.HinderCard(pass);
		scn.AddThreats(1);

		assertInZone(Zone.SHADOW_CHARACTERS, gollum1);
		assertInZone(Zone.DISCARD, gollum2);

		scn.ShadowPlayCard(twofaced);

		// Accept optional replacement
		scn.ShadowChooseYes();

		// Choose gollum1 to replace (auto-selected if only one)
		// Choose gollum2 from discard (auto-selected if only one)

		// Gollum1 should be in discard, gollum2 should be in play
		assertInZone(Zone.DISCARD, gollum1);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum2);
		assertEquals(0, scn.GetThreats());
	}

	@Test
	public void TwofacedReplacementKeepsBorneCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var gollum1 = scn.GetShadowCard("gollum1");
		var gollum2 = scn.GetShadowCard("gollum2");
		var letdeal = scn.GetShadowCard("letdeal");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(gollum1, runner);
		scn.AttachCardsTo(gollum1, letdeal);
		scn.MoveCardsToDiscard(gollum2);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.HinderCard(pass);
		scn.AddThreats(1);

		assertAttachedTo(letdeal, gollum1);

		scn.ShadowPlayCard(twofaced);
		scn.ShadowAcceptOptionalTrigger();

		// Let Her Deal With Them should now be attached to gollum2
		assertInZone(Zone.DISCARD, gollum1);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum2);
		assertAttachedTo(letdeal, gollum2);
	}

	@Test
	public void TwofacedReplacementKeepsWounds() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var gollum1 = scn.GetShadowCard("gollum1");
		var gollum2 = scn.GetShadowCard("gollum2");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(gollum1, runner);
		scn.MoveCardsToDiscard(gollum2);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.AddWoundsToChar(gollum1, 2);
		scn.HinderCard(pass);
		scn.AddThreats(1);

		assertEquals(2, scn.GetWoundsOn(gollum1));

		scn.ShadowPlayCard(twofaced);
		scn.ShadowAcceptOptionalTrigger();

		// Gollum2 should have same wounds as gollum1 had
		assertInZone(Zone.SHADOW_CHARACTERS, gollum2);
		assertEquals(2, scn.GetWoundsOn(gollum2));
	}

	@Test
	public void TwofacedReplacementDuringSkirmishContinuesSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var shelob1 = scn.GetShadowCard("shelob1");
		var shelob2 = scn.GetShadowCard("shelob2");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(shelob1);
		scn.MoveCardsToDiscard(shelob2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, shelob1);
		scn.FreepsResolveSkirmish(aragorn);

		scn.HinderCard(pass);
		scn.AddThreats(1);

		// Aragorn is skirmishing shelob1
		assertTrue(scn.IsCharSkirmishing(shelob1));
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, shelob1);
		assertInZone(Zone.DISCARD, shelob2);
		scn.ShadowPlayCard(twofaced);
		scn.ShadowChooseYes();

		// Shelob2 should now be in skirmish
		assertInZone(Zone.DISCARD, shelob1);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob2);
		assertTrue(scn.IsCharSkirmishing(shelob2));

		// Skirmish should still be resolvable
		scn.PassCurrentPhaseActions();
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void TwofacedReplacementKeepsStrengthModifiers() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var gollummad = scn.GetShadowCard("gollummad"); // Has +3 strength trigger on assignment
		var gollum2 = scn.GetShadowCard("gollum2");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(gollummad);
		scn.MoveCardsToDiscard(gollum2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();

		int gollumBaseStrength = scn.GetStrength(gollummad);

		// Assign Aragorn to Gollum Mad Thing - triggers his +3 strength
		scn.FreepsAssignToMinions(aragorn, gollummad);

		// Freeps chose to give +3 rather than add burden
		scn.FreepsChoose("strength +3");

		assertEquals(gollumBaseStrength + 3, scn.GetStrength(gollummad));

		scn.FreepsResolveSkirmish(aragorn);

		scn.HinderCard(pass);
		scn.AddThreats(1);
		scn.FreepsPass();

		scn.ShadowPlayCard(twofaced);
		scn.ShadowAcceptOptionalTrigger();

		// Gollum2 should retain the +3 strength modifier
		assertInZone(Zone.SHADOW_CHARACTERS, gollum2);
		int gollum2BaseStrength = 5; // Gollum, Nasty Treacherous Creature is strength 5
		assertEquals(gollum2BaseStrength + 3, scn.GetStrength(gollum2));
	}

	@Test
	public void TwofacedWithMultipleGollumMinionsAllowsChoice() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var pass = scn.GetShadowCard("pass");
		var gollum1 = scn.GetShadowCard("gollum1");
		var gollum2 = scn.GetShadowCard("gollum2");
		var shelob1 = scn.GetShadowCard("shelob1");
		var shelob2 = scn.GetShadowCard("shelob2");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(gollum1, shelob1);
		scn.MoveCardsToDiscard(gollum2, shelob2);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.HinderCard(pass);
		scn.AddThreats(1);

		scn.ShadowPlayCard(twofaced);
		scn.ShadowAcceptOptionalTrigger();

		// Should have choice between gollum1 and shelob1
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoicesAvailable(gollum1, shelob1));

		// Choose Shelob to replace
		scn.ShadowChooseCard(shelob1);

		// Shelob2 should replace Shelob1
		assertInZone(Zone.DISCARD, shelob1);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob2);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum1); // Gollum unchanged
	}
}
