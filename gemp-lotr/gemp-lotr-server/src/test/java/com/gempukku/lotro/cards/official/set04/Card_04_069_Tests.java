package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("count", "4_69");
					// Legolas, Companion of the Ring: strength 6, vitality 3, no archer; stronger only at a battleground
					// or forest site, which no site of this path is
					put("legolas", "11_21");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion who is no Legolas, strength 6; +3 only against a Nazgul
					// My Axe Is Notched: [dwarven] support-area condition that takes a [dwarven] token each time Gimli
					// wins a skirmish (no Gimli is laid, so its own bonus reaches nobody)
					put("notched", "4_52");
					// Stout and Strong: [dwarven] support-area condition that takes [dwarven] tokens of its own; its
					// skirmish ability is used only on purpose
					put("stout", "4_57");
					// Agility: [elven] support-area condition that takes [elven] tokens of its own; its skirmish
					// ability is used only on purpose
					put("agility", "6_12");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger does nothing when cheated in
					// Uruk Pursuer: printed fierce, strength 5, vitality 3: Legolas beats it in the skirmish and again in
					// the fierce skirmish; its bar and its response reach no Hobbit here
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Legolas who is no archer, and an Uruk Savage he beats without killing it.
	protected VirtualTableScenario GetLegolasScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("count", "4_69");
					// Legolas, Companion of the Ring: strength 6; stronger only at a battleground or forest site
					put("legolas", "11_21");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Redhorn Pass 13_194 ("Culture tokens cannot be added, removed, or reinforced") in
	// place of a site no test reaches; the test lays it as the site after the first.
	protected VirtualTableScenario GetRedhornPassScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("count", "4_69");
					put("legolas", "11_21");    // Legolas, Companion of the Ring: strength 6; a mountain is no battleground or forest
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FinalCountStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Final Count
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * Each time Legolas wins a skirmish, you may place an [elven] token on this card.
		 * While you can spot X [elven] tokens on this card and the same number of [dwarven] tokens on My Axe Is Notched, Legolas is strength +X (limit +3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("count");

		assertEquals("Final Count", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "Each time Legolas wins a skirmish, you may place an [elven] token on this card": a "you may" on a trigger
	// is asked and may be declined (Q-0021; Q-0220: G).  The definition placed the token unasked.  (My Axe Is
	// Notched 4_52 has no "may" and places its token unasked.)
	// F-0460.
	@Test
	public void PlacingTheTokenIsOfferedAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLegolasScenario();

		var count = scn.GetFreepsCard("count");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(count);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();

		// Legolas has won: the offer is made, and declined.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(count));
		assertEquals(0, scn.GetCultureTokensOn(count));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(count));

		// A second move, and Legolas beats the same minion again: the offer is made again, and taken.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(count));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ATokenPlacedForAWinMatchesMyAxeIsNotchedAndMakesLegolasStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		var agility = scn.GetFreepsCard("agility");
		var legolas = scn.GetFreepsCard("legolas");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToSupportArea(count, notched, agility);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(agility, 2);
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertEquals(2, scn.GetCultureTokensOn(agility));
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, pursuer);
		scn.FreepsResolveSkirmish(legolas);

		// One [dwarven] token on My Axe Is Notched and none here yet: +0.
		assertEquals(0, scn.GetCultureTokensOn(count));
		assertEquals(6, scn.GetStrength(legolas));
		scn.PassSkirmishActions();

		// 6 against 5: Legolas wins, and the token is offered and taken.  It goes on this card, not on Agility.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(count));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(2, scn.GetCultureTokensOn(agility));
		// One of each: Legolas is strength +1 at once.
		assertEquals(7, scn.GetStrength(legolas));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnlyLegolasWinningASkirmishOffersAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var count = scn.GetFreepsCard("count");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(count);
		scn.MoveCompanionsToTable(legolas, arwen);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		// Strength 8 beats Legolas's 6 without overwhelming him; he takes 2 wounds (damage +1) of his 3 and lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, savage, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, runner},
				new PhysicalCardImpl[]{legolas, savage});

		// An Elf companion who is no Legolas wins: nothing is offered.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(count));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Legolas loses, and lives: nothing is offered.
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsResolveSkirmish(legolas);
		assertEquals(8, scn.GetStrength(savage));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(count));
		assertEquals(0, scn.GetCultureTokensOn(count));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhereNoCultureTokenCanBeAddedTheTokenIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornPassScenario();

		var count = scn.GetFreepsCard("count");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(count);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("redhorn"));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		assertEquals("Redhorn Pass", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);

		// 6 against 5: Legolas wins, as he does where the token is offered.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(count));
		assertEquals(0, scn.GetCultureTokensOn(count));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LegolasIsStrengthPlusTheNumberOfTokensMatchedOnBothCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCardsToSupportArea(count, notched);
		scn.MoveCompanionsToTable(legolas, arwen);

		scn.StartGame();
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(arwen));

		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(notched, 1);
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertEquals(7, scn.GetStrength(legolas));
		// Another Elf companion, who is no Legolas, gains nothing.
		assertEquals(6, scn.GetStrength(arwen));

		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(notched, 1);
		assertEquals(2, scn.GetCultureTokensOn(count));
		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertEquals(8, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(arwen));
	}

	@Test
	public void NoBonusWithoutDwarvenTokensOnMyAxeIsNotchedEvenWithDwarvenTokensElsewhere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		var stout = scn.GetFreepsCard("stout");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(count, notched, stout);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		assertEquals(6, scn.GetStrength(legolas));
		scn.AddTokensToCard(count, 2);
		// [dwarven] tokens on a [dwarven] condition that is not My Axe Is Notched.
		scn.AddTokensToCard(stout, 2);
		assertEquals(2, scn.GetCultureTokensOn(count));
		assertEquals(2, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetCultureTokensOn(notched));
		assertEquals(6, scn.GetStrength(legolas));

		// The control: the same number on My Axe Is Notched.
		scn.AddTokensToCard(notched, 2);
		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertEquals(8, scn.GetStrength(legolas));
	}

	@Test
	public void ElvenTokensOnAnotherCardDoNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		var agility = scn.GetFreepsCard("agility");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(count, notched, agility);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		assertEquals(6, scn.GetStrength(legolas));
		scn.AddTokensToCard(notched, 2);
		// [elven] tokens on an [elven] condition that is not this one.
		scn.AddTokensToCard(agility, 2);
		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertEquals(2, scn.GetCultureTokensOn(agility));
		assertEquals(0, scn.GetCultureTokensOn(count));
		assertEquals(6, scn.GetStrength(legolas));

		// The control: the same number here.
		scn.AddTokensToCard(count, 2);
		assertEquals(2, scn.GetCultureTokensOn(count));
		assertEquals(8, scn.GetStrength(legolas));
	}

	@Test
	public void TheBonusStopsAtPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(count, notched);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		assertEquals(6, scn.GetStrength(legolas));
		scn.AddTokensToCard(count, 3);
		scn.AddTokensToCard(notched, 3);
		assertEquals(3, scn.GetCultureTokensOn(count));
		assertEquals(3, scn.GetCultureTokensOn(notched));
		assertEquals(9, scn.GetStrength(legolas));

		// Four of each: still +3.
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(notched, 1);
		assertEquals(4, scn.GetCultureTokensOn(count));
		assertEquals(4, scn.GetCultureTokensOn(notched));
		assertEquals(9, scn.GetStrength(legolas));
	}

	@Test
	public void TheBonusIsTheLowerOfTheTwoCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(count, notched);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		assertEquals(6, scn.GetStrength(legolas));
		// The ruling's example: two [elven] tokens here and three [dwarven] tokens on My Axe Is Notched.
		scn.AddTokensToCard(count, 2);
		scn.AddTokensToCard(notched, 3);
		assertEquals(2, scn.GetCultureTokensOn(count));
		assertEquals(3, scn.GetCultureTokensOn(notched));
		assertEquals(8, scn.GetStrength(legolas));

		// The other way round: three here and two there.
		scn.AddTokensToCard(count, 1);
		scn.RemoveTokensFromCard(notched, 1);
		assertEquals(3, scn.GetCultureTokensOn(count));
		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertEquals(8, scn.GetStrength(legolas));
	}
}
