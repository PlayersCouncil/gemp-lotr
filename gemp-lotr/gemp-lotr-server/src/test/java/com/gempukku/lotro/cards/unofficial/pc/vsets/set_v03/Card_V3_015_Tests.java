package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("twofaced", "103_15");
					put("twofaced2", "103_15");
					put("twofaced3", "103_15");
					put("burden", "7_60");          // Heavy Burden: [gollum] Shadow condition in the support area; a regroup ability only
					put("burden2", "7_60");
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: strength 5, vitality 4; a regroup ability only
					put("gollumcopy", "5_24");
					put("vile", "7_59");            // Gollum, Vile Creature: a skirmish ability never used here
					put("villain", "6_40");         // Gollum, Old Villain: a skirmish ability never used here
					put("dark", "9_28");            // Gollum, Dark as Darkness: only ever in the other player's discard pile
					put("mad", "10_21");            // Gollum, Mad Thing: to assign him the Free Peoples player makes him strength +3 until the regroup phase or adds a burden
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: a [wraith] minion of the same title
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: strength 8, vitality 8, enduring, fierce
					put("shelobcopy", "8_26");
					put("ladyship", "10_23");       // Shelob, Her Ladyship: enduring, fierce; "When you play Shelob, if you can spot Gollum, you may spot a companion..."
					put("letdeal", "7_63");         // Let Her Deal With Them: [gollum] condition borne by a minion, who is damage +1
					put("runner", "1_178");         // Goblin Runner: [moria], strength 5, vitality 1
					put("runner2", "1_178");
					put("lemenya", "1_232");        // Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul; her strength bonus needs a [wraith] condition on a companion
					put("hunter", "6_86");          // Ulaire Lemenya, Winged Hunter: a Lemenya of another subtitle, only ever in a discard pile

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("glorfindel", "9_16");      // Glorfindel, Revealed in Wrath: strength 9, vitality 3; a skirmish ability never used here
					put("smeagol", "5_29");         // Smeagol, Slinker: the other player's [gollum] companion; a skirmish ability never used here
					put("noser", "5_28");           // Smeagol, Old Noser: a Smeagol of another subtitle, only ever in a discard pile
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
		 * Subtype: Shadow, Maneuver or Skirmish
		 * Game Text: Shadow or Maneuver or Skirmish: Restore a [gollum] card.  Then you may spot a [gollum] minion and remove a threat to replace it with another minion from your discard pile with the same title (keep all tokens and borne cards).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("twofaced");

		assertEquals("Two-faced", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RestoresOneHinderedGollumCardOfEitherPlayersAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var burden = scn.GetShadowCard("burden");
		var burden2 = scn.GetShadowCard("burden2");
		var shelob = scn.GetShadowCard("shelob");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");

		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToSupportArea(burden, burden2);
		scn.MoveCardsToHand(twofaced);

		scn.StartGame();
		// The one Gollum on this table is the [wraith] minion, so the [gollum] minion is Shelob.
		scn.MoveMinionsToTable(shelob, wraithgollum, runner);
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.HinderCard(shelob, wraithgollum, runner, burden, aragorn, smeagol);
		int twilight = scn.GetTwilight();
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(twofaced));

		scn.ShadowPlayCard(twofaced);
		assertEquals(twilight - 2, scn.GetTwilight());

		// A hindered [gollum] minion, a hindered [gollum] condition, and the other player's hindered [gollum]
		// companion: the text says "a [gollum] card" and no more.
		assertTrue(scn.ShadowHasCardChoiceAvailable(shelob, burden, smeagol));
		// A hindered Gollum who is a [wraith] minion, a hindered [moria] minion, a hindered [gondor] companion,
		// and a [gollum] condition that is not hindered.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(wraithgollum, runner, aragorn, burden2));
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(smeagol);
		assertFalse(scn.IsHindered(smeagol));
		assertTrue(scn.IsHindered(shelob));
		assertTrue(scn.IsHindered(burden));
		assertTrue(scn.IsHindered(wraithgollum));
		assertTrue(scn.IsHindered(runner));
		assertTrue(scn.IsHindered(aragorn));

		assertInZone(Zone.DISCARD, twofaced);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayableAtTheShadowPlayersShadowManeuverAndSkirmishActionsAndAtNoOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var twofaced2 = scn.GetShadowCard("twofaced2");
		var twofaced3 = scn.GetShadowCard("twofaced3");
		var burden = scn.GetShadowCard("burden");
		var burden2 = scn.GetShadowCard("burden2");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(burden, burden2);
		scn.MoveCardsToHand(twofaced, twofaced2, twofaced3);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.HinderCard(burden, burden2);

		// Shadow phase.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(twofaced));
		scn.ShadowPass();

		// Maneuver phase.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(twofaced));
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(twofaced);
		scn.ShadowChooseCard(burden);
		assertFalse(scn.IsHindered(burden));
		assertTrue(scn.IsHindered(burden2));
		assertEquals(twilight - 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, twofaced);
		scn.FreepsPass();
		scn.ShadowPass();

		// Archery phase.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(twofaced2));
		scn.ShadowPass();

		// Assignment phase.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(twofaced2));
		scn.ShadowPass();

		// A skirmish.
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(twofaced2));
		scn.ShadowPlayCard(twofaced2);
		// One hindered [gollum] card is left, and is restored without a choice.
		assertFalse(scn.IsHindered(burden2));
		assertEquals(twilight - 4, scn.GetTwilight());
		assertInZone(Zone.DISCARD, twofaced2);
		scn.FreepsPass();
		scn.ShadowPass();

		// Regroup phase.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(twofaced3));
	}

	@Test
	public void ReplacesTheSpottedGollumMinionWithAnotherOfItsTitleFromMyDiscardPileWhichKeepsItsWoundsAndWhatItBore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var gollum = scn.GetShadowCard("gollum");
		var gollumcopy = scn.GetShadowCard("gollumcopy");
		var vile = scn.GetShadowCard("vile");
		var villain = scn.GetShadowCard("villain");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var shelob = scn.GetShadowCard("shelob");
		var ladyship = scn.GetShadowCard("ladyship");
		var letdeal = scn.GetShadowCard("letdeal");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var othersGollum = scn.GetFreepsCard("dark");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");

		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToDiscard(vile, villain, wraithgollum, ladyship, runner2, othersGollum, gollumcopy);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob, runner);
		scn.AttachCardsTo(gollum, letdeal);
		scn.AddWoundsToChar(gollum, 2);
		scn.AddThreats(2);
		scn.SetTwilight(5);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertInZone(Zone.DISCARD, vile);

		// Nothing is hindered, so nothing is restored, and the event goes on to its second sentence.
		scn.ShadowPlayCard(twofaced);
		assertTrue(scn.ShadowDecisionAvailable("remove a threat"));
		scn.ShadowChooseYes();
		assertEquals(1, scn.GetThreats());

		// Both of my [gollum] minions; not my [moria] minion, and not the other player's [gollum] companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, smeagol));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gollum);

		// Any minion titled Gollum in my discard pile, the [wraith] one included: the text asks for the title
		// and no culture.  Not a Shelob, not a Goblin Runner, not a Gollum in the other player's discard pile;
		// and, as the game has it, not a second copy of the very Gollum being replaced.
		assertTrue(scn.ShadowHasCardChoiceAvailable(vile, villain, wraithgollum));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ladyship, runner2, othersGollum, gollumcopy));
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(vile);

		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertInZone(Zone.DISCARD, gollum);
		assertEquals(2, scn.GetWoundsOn(vile));
		assertAttachedTo(letdeal, vile);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertInZone(Zone.DISCARD, villain, wraithgollum, ladyship, gollumcopy);
		// Only the event's own 2: the new Gollum is not played and is not paid for.
		assertEquals(twilight - 2, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DISCARD, twofaced);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheReplacementMayBeDeclinedAndNothingIsRemovedOrReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var gollum = scn.GetShadowCard("gollum");
		var vile = scn.GetShadowCard("vile");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToDiscard(vile);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.AddThreats(1);
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.ShadowPlayCard(twofaced);
		assertTrue(scn.ShadowDecisionAvailable("remove a threat"));
		scn.ShadowChooseNo();

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertInZone(Zone.DISCARD, vile);
		assertInZone(Zone.DISCARD, twofaced);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoThreatToRemoveTheReplacementIsNotOfferedAndWithOneItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var twofaced2 = scn.GetShadowCard("twofaced2");
		var burden = scn.GetShadowCard("burden");
		var gollum = scn.GetShadowCard("gollum");
		var vile = scn.GetShadowCard("vile");

		scn.MoveCardsToHand(twofaced, twofaced2);
		scn.MoveCardsToSupportArea(burden);
		scn.MoveCardsToDiscard(vile);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.HinderCard(burden);
		assertEquals(0, scn.GetThreats());

		// The first sentence is carried out, and the second is not put to the Shadow player.
		scn.ShadowPlayCard(twofaced);
		assertFalse(scn.IsHindered(burden));
		assertInZone(Zone.DISCARD, twofaced);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The same board with a threat on it.
		scn.AddThreats(1);
		scn.ShadowPlayCard(twofaced2);
		assertTrue(scn.ShadowDecisionAvailable("remove a threat"));
		scn.ShadowChooseYes();
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertInZone(Zone.DISCARD, gollum);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoMinionOfTheGollumCultureInPlayTheReplacementIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var vile = scn.GetShadowCard("vile");

		scn.MoveCardsToHand(twofaced);
		// A Gollum and a Goblin Runner wait in the discard pile for the two minions in play.
		scn.MoveCardsToDiscard(vile, runner2);

		scn.StartGame();
		scn.MoveMinionsToTable(wraithgollum, runner);
		scn.AddThreats(1);
		scn.SetTwilight(5);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(twofaced));
		scn.ShadowPlayCard(twofaced);

		// Nothing was hindered and nobody can be spotted: the event is paid for and does nothing.
		assertEquals(twilight - 2, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, wraithgollum, runner);
		assertInZone(Zone.DISCARD, vile, runner2, twofaced);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RestoringTheOnlyGollumMinionComesFirstSoThatItCanThenBeSpottedAndReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var gollum = scn.GetShadowCard("gollum");
		var villain = scn.GetShadowCard("villain");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToDiscard(villain);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.AddThreats(1);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// Hindered, Gollum cannot be spotted as a [gollum] minion.
		scn.HinderCard(gollum);
		assertTrue(scn.IsHindered(gollum));

		scn.ShadowPlayCard(twofaced);
		assertFalse(scn.IsHindered(gollum));
		assertTrue(scn.ShadowDecisionAvailable("remove a threat"));
		scn.ShadowChooseYes();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, villain);
		assertFalse(scn.IsHindered(villain));
		assertInZone(Zone.DISCARD, gollum);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ReplacedInItsSkirmishTheNewMinionFightsOnInItsPlaceAndHasNotBeenPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var shelob = scn.GetShadowCard("shelob");
		var ladyship = scn.GetShadowCard("ladyship");
		var gollum = scn.GetShadowCard("gollum");
		var vile = scn.GetShadowCard("vile");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.MoveCardsToHand(twofaced);
		// One minion of each title waits in the discard pile.
		scn.MoveCardsToDiscard(ladyship, vile);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum);
		scn.AddWoundsToChar(shelob, 1);
		scn.AddThreats(1);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, shelob));
		assertTrue(scn.ShadowPlayAvailable(twofaced));

		scn.ShadowPlayCard(twofaced);
		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(shelob, gollum));
		scn.ShadowChooseCard(shelob);

		// The one Shelob in the discard pile comes in without a choice; the Gollum there stays.
		assertInZone(Zone.SHADOW_CHARACTERS, ladyship);
		assertInZone(Zone.DISCARD, shelob);
		assertInZone(Zone.DISCARD, vile);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, ladyship));
		assertFalse(scn.IsCharSkirmishing(shelob));
		assertEquals(1, scn.GetWoundsOn(ladyship));
		assertEquals(0, scn.GetThreats());

		// Her Ladyship says "When you play Shelob, if you can spot Gollum, you may spot a companion": Gollum is
		// there, and nothing is offered, because she was not played.  Nor were her 6 and 2 for roaming paid.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(twilight - 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The skirmish goes on: 8 + 2 for an enduring Shelob's one wound, against Aragorn's 8.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(ladyship));
		// On to the fierce assignment, where Her Ladyship is the one to be assigned.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheNewMinionStandsWhereTheOldOneStoodAssignedToTheSameCompanionAndWithWhatHadBeenDoneToIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var mad = scn.GetShadowCard("mad");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToDiscard(gollum);

		scn.StartGame();
		scn.MoveMinionsToTable(mad, runner);
		scn.AddThreats(1);
		scn.SkipToAssignments();

		assertEquals(5, scn.GetStrength(mad));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, runner },
				new PhysicalCardImpl[] { glorfindel, mad }
		);
		// Mad Thing's own text: to assign him, the Free Peoples player makes him strength +3 until the
		// regroup phase.
		scn.FreepsChoose("strength +3");
		assertEquals(8, scn.GetStrength(mad));

		// Aragorn's skirmish against the Runner; Gollum is assigned to Glorfindel and waiting.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharAssignedAgainst(glorfindel, mad));

		scn.ShadowPlayCard(twofaced);
		scn.ShadowChooseYes();

		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertInZone(Zone.DISCARD, mad);
		assertTrue(scn.IsCharAssignedAgainst(glorfindel, gollum));
		// 5 printed on the new Gollum + the 3 given to the old one
		assertEquals(8, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNothingInMyDiscardPileThatCouldReplaceAGollumMinionInPlayTheReplacementIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var twofaced2 = scn.GetShadowCard("twofaced2");
		var shelob = scn.GetShadowCard("shelob");
		var shelobcopy = scn.GetShadowCard("shelobcopy");
		var ladyship = scn.GetShadowCard("ladyship");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var vile = scn.GetShadowCard("vile");
		var noser = scn.GetShadowCard("noser");
		var smeagol = scn.GetFreepsCard("smeagol");

		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(twofaced, twofaced2);
		// In my discard pile: a Gollum, whose title is in play only on a [wraith] minion; a Smeagol, whose
		// title is in play only on a [gollum] companion; and a second copy of the very Shelob in play.
		scn.MoveCardsToDiscard(vile, noser, shelobcopy);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, wraithgollum);
		scn.AddThreats(2);
		scn.SetTwilight(5);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(twofaced));
		scn.ShadowPlayCard(twofaced);

		// Shelob could be spotted and a threat removed, but nothing there could take her place: as the game
		// has it, the Shadow player is not asked.
		assertEquals(twilight - 2, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, shelob, wraithgollum);
		assertInZone(Zone.DISCARD, vile, noser, shelobcopy, twofaced);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The same board with a Shelob of another subtitle in the discard pile: asked, and she is the one
		// to come in.
		scn.MoveCardsToDiscard(ladyship);
		scn.ShadowPlayCard(twofaced2);
		assertTrue(scn.ShadowDecisionAvailable("remove a threat"));
		scn.ShadowChooseYes();
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, ladyship, wraithgollum);
		assertInZone(Zone.DISCARD, shelob, shelobcopy, vile, noser);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AGollumMinionThatNothingInMyDiscardPileCouldReplaceIsNotOfferedToBeSpottedAndTheOneThatCouldBeIsTakenUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var gollum = scn.GetShadowCard("gollum");
		var gollumcopy = scn.GetShadowCard("gollumcopy");
		var shelob = scn.GetShadowCard("shelob");
		var ladyship = scn.GetShadowCard("ladyship");
		var lemenya = scn.GetShadowCard("lemenya");
		var hunter = scn.GetShadowCard("hunter");

		scn.MoveCardsToHand(twofaced);
		// A Shelob of another subtitle for the Shelob in play, and a Lemenya of another subtitle for the
		// Lemenya in play.  For Gollum there is only a second copy of himself, which is not another Gollum.
		scn.MoveCardsToDiscard(ladyship, hunter, gollumcopy);

		scn.StartGame();
		// Every [gollum] minion is a Gollum or a Shelob.  The Nazgul could be replaced as Shelob could, and is
		// a [wraith] minion.
		scn.MoveMinionsToTable(gollum, shelob, lemenya);
		scn.AddWoundsToChar(shelob, 1);
		scn.AddThreats(1);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, gollum, shelob, lemenya);

		scn.ShadowPlayCard(twofaced);
		assertTrue(scn.ShadowDecisionAvailable("remove a threat"));
		scn.ShadowChooseYes();
		assertEquals(0, scn.GetThreats());

		// Replacing from the discard pile is treated as playing from it: Gollum, whom nothing there could
		// replace, is not offered to be spotted, and the Nazgul is no [gollum] minion.  That leaves Shelob, and
		// with one minion to spot nobody is asked which: either of the others offered would have put the choice.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, ladyship, gollum, lemenya);
		assertInZone(Zone.DISCARD, shelob, hunter, gollumcopy);
		assertEquals(1, scn.GetWoundsOn(ladyship));
		assertInZone(Zone.DISCARD, twofaced);
	}

	@Test
	public void TheCardTheOldMinionBoreGivesTheNewMinionItsBonusOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var gollum = scn.GetShadowCard("gollum");
		var vile = scn.GetShadowCard("vile");
		var letdeal = scn.GetShadowCard("letdeal");

		scn.MoveCardsToHand(twofaced);
		scn.MoveCardsToDiscard(vile);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.AttachCardsTo(gollum, letdeal);
		scn.AddThreats(1);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// Let Her Deal With Them: bearer is damage +1.  Neither Gollum has damage of his own.
		assertEquals(1, scn.GetKeywordCount(gollum, Keyword.DAMAGE));

		scn.ShadowPlayCard(twofaced);
		scn.ShadowChooseYes();

		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertInZone(Zone.DISCARD, gollum);
		assertAttachedTo(letdeal, vile);
		assertEquals(1, scn.GetKeywordCount(vile, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheNewMinionHasLeftPlayThereIsNoGollumMinionLeftToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twofaced = scn.GetShadowCard("twofaced");
		var twofaced2 = scn.GetShadowCard("twofaced2");
		var gollum = scn.GetShadowCard("gollum");
		var vile = scn.GetShadowCard("vile");
		var villain = scn.GetShadowCard("villain");

		scn.MoveCardsToHand(twofaced, twofaced2);
		scn.MoveCardsToDiscard(vile, villain);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.AddThreats(2);
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.ShadowPlayCard(twofaced);
		scn.ShadowChooseYes();
		scn.ShadowChooseCard(vile);
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertInZone(Zone.DISCARD, gollum);
		assertEquals(1, scn.GetThreats());

		// The new Gollum leaves play.  My discard pile now holds three Gollums, any of which could replace
		// another, and a threat is there to remove; but no Gollum is in play to be spotted.
		scn.MoveCardsToDiscard(vile);
		assertInZone(Zone.DISCARD, vile, gollum, villain);

		scn.ShadowPlayCard(twofaced2);
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DISCARD, twofaced2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
