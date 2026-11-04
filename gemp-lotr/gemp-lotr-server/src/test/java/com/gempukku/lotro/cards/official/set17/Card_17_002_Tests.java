package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balin", "17_2");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetOrcCardsInTheDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balin", "17_2");
					put("guard1", "1_7");           // Dwarf Guard: a Dwarf companion who is not unique; no text in play
					put("guard2", "1_7");

					// Prowling Orc: an [orc] Orc, cost 3, site 4; lurker, and a skirmish ability
					put("prowler", "11_136");
					// [orc] cards of two types for the discard pile, where neither does anything
					put("underling", "11_125");     // Isengard Underling: minion
					put("demoralized", "11_114");   // Demoralized: condition
					// Goblin Runner: an Orc of the [moria] culture, cost 1; its "may add (2)" comes without asking
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Dwarves of each kind, minions to exert, and the cards for the play and its token.
	protected VirtualTableScenario GetDwarvesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balin", "17_2");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; his skirmish ability is not used
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion; strength +2 against an Orc
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf
					// Stout and Strong: another [dwarven] support-area condition, cost 0; its texts need a Dwarf to
					// win a skirmish, or are used only on purpose
					put("stout", "4_57");
					// Heirs of Gondor: [gondor] support-area condition that carries [gondor] tokens; its skirmish
					// ability is not used
					put("heirs", "13_69");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc of vitality 1, who cannot exert

					put("prowler", "11_136");   // Prowling Orc: an [orc] Orc, cost 3; a skirmish ability, not used
					put("underling", "11_125"); // Isengard Underling: an [orc] minion for the discard pile
					// Half-troll of Far Harad: an [orc] minion who is no Orc, cost 3; its trigger needs (2) at the
					// start of the assignment phase
					put("halftroll", "14_12");
					// Demoralized: [orc] condition, in the Free Peoples player's own discard pile
					put("demoralized", "11_114");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Redhorn Pass in each adventure deck in place of site 9, which no test reaches.
	protected VirtualTableScenario GetRedhornPassScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balin", "17_2");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					// Redhorn Pass: Shadows-block site. "Culture tokens cannot be added, removed, or reinforced."
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BalinAvengedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Balin Avenged
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this condition, you may add a [dwarven] token here. While you can spot 4 [dwarven] tokens and 2 Dwarves, each [orc] Orc gains this text "To play, remove an [orc] card from your discard pile from the game."<br><b>Skirmish:</b> Discard this condition to exert a minion for each Dwarf you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("balin");

		assertEquals("Balin Avenged", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFourTokensAndTwoDwarvesAnOrcIsPlayedOnlyByRemovingAnOrcCardInItsOwnersDiscardPileFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOrcCardsInTheDiscardPileScenario();

		var balin = scn.GetFreepsCard("balin");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveCardsToSupportArea(balin);
		scn.AddTokensToCard(balin, 4);

		var prowler = scn.GetShadowCard("prowler");
		var underling = scn.GetShadowCard("underling");
		var demoralized = scn.GetShadowCard("demoralized");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCardsToHand(prowler);
		scn.MoveCardsToDiscard(underling, demoralized, runner);

		scn.StartGame();
		assertEquals(4, scn.GetCultureTokensOn(balin));
		assertInDiscard(underling, demoralized, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetCultureTokensOn(balin));
		assertTrue(scn.ShadowPlayAvailable(prowler));

		// One of the two [orc] cards in his discard pile is to be removed; the Orc of another culture is no
		// candidate.
		scn.ShadowPlayCard(prowler);
		assertTrue(scn.ShadowHasCardChoiceAvailable(underling, demoralized));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(demoralized);
		assertInZone(Zone.REMOVED, demoralized);
		assertInDiscard(underling, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, prowler);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOrcCardInItsOwnersDiscardPileAnOrcCannotBePlayedAndAnOrcOfAnotherCultureCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOrcCardsInTheDiscardPileScenario();

		var balin = scn.GetFreepsCard("balin");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveCardsToSupportArea(balin);
		scn.AddTokensToCard(balin, 4);

		var prowler = scn.GetShadowCard("prowler");
		var runnerInHand = scn.GetShadowCard("runner1");
		var runnerInDiscard = scn.GetShadowCard("runner2");
		scn.MoveCardsToHand(prowler, runnerInHand);
		// A [moria] Orc is the only card in his discard pile: nothing there pays for an [orc] Orc.
		scn.MoveCardsToDiscard(runnerInDiscard);

		scn.StartGame();
		assertInDiscard(runnerInDiscard);
		assertInHand(prowler, runnerInHand);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(prowler));
		// The pool would pay for either; the [moria] Orc has no such text to pay for.
		assertTrue(scn.ShadowPlayAvailable(runnerInHand));

		scn.ShadowPlayCard(runnerInHand);
		assertInZone(Zone.SHADOW_CHARACTERS, runnerInHand);
		assertInDiscard(runnerInDiscard);
		assertInHand(prowler);
		assertFalse(scn.ShadowPlayAvailable(prowler));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
	@Test
	public void WhenPlayedItMayAddADwarvenTokenToItselfAndAnotherConditionPlayedAfterItIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarvesScenario();

		var balin = scn.GetFreepsCard("balin");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCardsToHand(balin, stout);

		scn.StartGame();

		assertInHand(balin, stout);
		scn.FreepsPlayCard(balin);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(balin));
		assertEquals(0, scn.GetCultureTokensOn(balin));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.SUPPORT, balin);
		assertEquals(1, scn.GetCultureTokensOn(balin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [dwarven] condition played afterwards sets nothing off.
		scn.FreepsPlayCard(stout);

		assertInZone(Zone.SUPPORT, stout);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(balin));
		assertEquals(1, scn.GetCultureTokensOn(balin));
		assertEquals(0, scn.GetCultureTokensOn(stout));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheTokenLeavesItWithNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarvesScenario();

		var balin = scn.GetFreepsCard("balin");
		scn.MoveCardsToHand(balin);

		scn.StartGame();

		assertInHand(balin);
		scn.FreepsPlayCard(balin);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(balin));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, balin);
		assertEquals(0, scn.GetCultureTokensOn(balin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhereNoTokenCanBeAddedTheTokenIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornPassScenario();

		var balin = scn.GetFreepsCard("balin");
		var redhorn = scn.GetShadowSite("redhorn");
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveCardToAdventurePath(redhorn);
		scn.SkipToSite(2);

		// The Free Peoples player's next fellowship phase, at Redhorn Pass.
		assertSame(redhorn, scn.GetCurrentSite());
		assertInHand(balin);
		scn.FreepsPlayCard(balin);

		assertInZone(Zone.SUPPORT, balin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(balin));
		assertEquals(0, scn.GetCultureTokensOn(balin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithThreeDwarvenTokensAnOrcCostsNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarvesScenario();

		var balin = scn.GetFreepsCard("balin");
		var heirs = scn.GetFreepsCard("heirs");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveCardsToSupportArea(balin, heirs);

		var prowler = scn.GetShadowCard("prowler");
		var underling = scn.GetShadowCard("underling");
		scn.MoveCardsToHand(prowler);
		scn.MoveCardsToDiscard(underling);

		scn.StartGame();
		// Three [dwarven] tokens, and a [gondor] token that is not one of them.
		scn.AddTokensToCard(balin, 3);
		scn.AddTokensToCard(heirs, 1);
		scn.SetTwilight(10);
		assertEquals(3, scn.GetCultureTokensOn(balin));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertInDiscard(underling);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(prowler));
		scn.ShadowPlayCard(prowler);

		assertInZone(Zone.SHADOW_CHARACTERS, prowler);
		assertInDiscard(underling);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneDwarfAnOrcCostsNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarvesScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(balin);

		var prowler = scn.GetShadowCard("prowler");
		var underling = scn.GetShadowCard("underling");
		scn.MoveCardsToHand(prowler);
		scn.MoveCardsToDiscard(underling);

		scn.StartGame();
		// Four tokens, and two companions beside the Ring-bearer, one of them no Dwarf.
		scn.AddTokensToCard(balin, 4);
		scn.SetTwilight(10);
		assertEquals(4, scn.GetCultureTokensOn(balin));
		assertInDiscard(underling);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(prowler));
		scn.ShadowPlayCard(prowler);

		assertInZone(Zone.SHADOW_CHARACTERS, prowler);
		assertInDiscard(underling);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ADwarfAllyCountsAndOnlyAnOrcOfTheOrcCultureIsChargedFromItsOwnersPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarvesScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var stout = scn.GetFreepsCard("stout");
		var fpDemoralized = scn.GetFreepsCard("demoralized");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(balin, stout);
		// An [orc] card in the Free Peoples player's discard pile pays for nothing of the Shadow player's.
		scn.MoveCardsToDiscard(fpDemoralized);

		var prowler = scn.GetShadowCard("prowler");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(prowler, halftroll);
		scn.MoveCardsToDiscard(runner);

		scn.StartGame();
		// The four [dwarven] tokens are spotted wherever they lie: two here, two on another [dwarven] condition.
		scn.AddTokensToCard(balin, 2);
		scn.AddTokensToCard(stout, 2);
		scn.SetTwilight(10);
		assertEquals(2, scn.GetCultureTokensOn(balin));
		assertEquals(2, scn.GetCultureTokensOn(stout));
		assertInDiscard(fpDemoralized, runner);
		assertInHand(prowler, halftroll);
		scn.FreepsPass();

		// Gimli and the ally Grimir are two Dwarves: the [orc] Orc has nothing in its owner's pile to remove.
		assertFalse(scn.ShadowPlayAvailable(prowler));
		// An [orc] minion who is no Orc gains nothing.
		assertTrue(scn.ShadowPlayAvailable(halftroll));
		scn.ShadowPlayCard(halftroll);

		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertInDiscard(fpDemoralized, runner);
		assertInHand(prowler);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsAMinionForEachDwarfAndMayExertOneTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarvesScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(balin);

		var troop = scn.GetShadowCard("troop");
		var party = scn.GetShadowCard("party");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, balin);
		scn.MoveMinionsToTable(troop, party, runner);
		scn.SkipToAssignments();
		// The Raiding Party and the Runner are left unassigned.
		scn.FreepsAssignToMinions(gimli, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.FreepsActionAvailable(balin));
		scn.FreepsUseCardAction(balin);

		// Three Dwarves (Gimli, Farin and the ally Grimir; Aragorn is none): three exertions, one at a time.  The
		// Runner, of vitality 1, cannot exert.
		assertInDiscard(balin);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, party));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(party);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, party));
		scn.FreepsChooseCard(party);

		// The Raiding Party is exhausted now: the third goes to the Troop unasked.
		assertEquals(2, scn.GetWoundsOn(party));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhaseAndWithTwoDwarvesExertsTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarvesScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveCardsToSupportArea(balin);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, balin);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsDecisionAvailable("Play Assignment action or Pass"));
		assertFalse(scn.FreepsActionAvailable(balin));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.FreepsActionAvailable(balin));
		scn.FreepsUseCardAction(balin);

		// The Troop is the only minion: both exertions are its, unasked.
		assertInDiscard(balin);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoMinionAbleToExertTheSkirmishAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarvesScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveCardsToSupportArea(balin);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, balin);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.FreepsActionAvailable(balin));
		scn.FreepsUseCardAction(balin);

		assertInDiscard(balin);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
