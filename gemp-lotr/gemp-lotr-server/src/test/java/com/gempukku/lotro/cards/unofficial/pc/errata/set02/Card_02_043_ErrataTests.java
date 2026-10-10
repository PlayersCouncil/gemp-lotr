package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_043_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "52_43");

					put("captain", "2_46");     // Uruk Captain: unique [isengard] Uruk-hai, strength 9, damage +1; a Shadow ability only used on purpose
					put("lurtz", "1_127");      // Lurtz, Servant of Isengard: unique [isengard] Uruk-hai, archer, damage +1; a maneuver ability only used on purpose
					put("shagrat", "10_99");    // Shagrat: unique [sauron] Uruk-hai, strength 13, damage +1; may exert a character when he loses a skirmish
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, not unique, strength 5, damage +1 and nothing else
					put("overseer", "3_65");    // Orc Overseer: unique [isengard] Orc; a regroup ability only used on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability only used on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, strength 3, vitality 4
					put("guard", "1_7");        // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an ally, so a character who is no companion; her text needs your site 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCourteousHalflingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "52_43");
					put("captain", "2_46");     // Uruk Captain: unique [isengard] Uruk-hai, strength 9, damage +1; a Shadow ability only used on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability only used on purpose
					put("guard1", "1_7");       // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("guard2", "1_7");
					put("sam", "1_311");        // these three stay in the draw deck, so that three cards could be discarded from it
					put("silinde", "1_60");
					put("guard3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",                        // Frodo, Courteous Halfling: with 3 unbound companions spotted, Shadow cards may not discard cards from the top of your draw deck
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetUrukHaiLurtzScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "52_43");
					put("lurtz", "11_194");     // Lurtz, Minion of the White Wizard: unique [uruk-hai] Uruk-hai, archer, damage +1; strength bonus only for exhausted companions
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LurtzsSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Lurtz's Sword
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be a unique Uruk-hai.
		 * 	If bearer is Lurtz, he is damage +1.
		 * 	Each time bearer wins a skirmish, the Free Peoples player must wound a companion or discard the top 3 cards of their draw deck.
		 */

		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");

		assertEquals("Lurtz's Sword", sword.getBlueprint().getTitle());
		assertNull(sword.getBlueprint().getSubtitle());
		assertTrue(sword.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sword.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, sword.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, sword.getBlueprint().getCardType());
		assertTrue(sword.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, sword.getBlueprint().getTwilightCost());
		assertEquals(2, sword.getBlueprint().getStrength());
	}

	@Test
	public void PlaysForOneOnAUniqueUrukhaiOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz = scn.GetShadowCard("lurtz");
		var shagrat = scn.GetShadowCard("shagrat");
		var savage = scn.GetShadowCard("savage");
		var overseer = scn.GetShadowCard("overseer");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, shagrat, savage, overseer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(sword));
		int twilight = scn.GetTwilight();
		assertEquals(13, scn.GetStrength(shagrat));

		scn.ShadowPlayCard(sword);
		// Unique Uruk-hai of two cultures; an Uruk-hai that is not unique and a unique Orc are not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lurtz, shagrat));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, overseer));
		scn.ShadowChooseCard(shagrat);

		assertAttachedTo(sword, shagrat);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(15, scn.GetStrength(shagrat));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void LurtzBearingItIsDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Lurtz is printed damage +1.
		assertEquals(1, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, lurtz);
		assertEquals(2, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABearerWhoIsNotLurtzGetsNoDamageBonusThoughLurtzIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var captain = scn.GetShadowCard("captain");
		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(captain, lurtz);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Each is printed damage +1.
		assertEquals(1, scn.GetKeywordCount(captain, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		scn.ShadowPlayCard(sword);
		scn.ShadowChooseCard(captain);
		assertAttachedTo(sword, captain);
		assertEquals(1, scn.GetKeywordCount(captain, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerWinningASkirmishTheFreePeoplesPlayerMayChooseToWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var captain = scn.GetShadowCard("captain");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(silinde);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		scn.AttachCardsTo(captain, sword);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();

		// The Captain, 9 + 2, beats Aragorn, 8: 2 wounds with damage +1, then the trigger.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsChoiceAvailable("Wound"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		assertFalse(scn.ShadowChoiceAvailable("Wound"));

		scn.FreepsChooseOption("Wound");
		// Any companion, in the skirmish or not, the Ring-bearer among them; Aragorn, exhausted, can still be wounded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sam, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(silinde));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerWinningASkirmishTheFreePeoplesPlayerMayChooseToDiscardTheTopThreeCardsOfTheirDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var captain = scn.GetShadowCard("captain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("guard");
		var top3 = scn.GetFreepsCard("silinde");
		var top4 = scn.GetFreepsCard("lurtz");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		scn.AttachCardsTo(captain, sword);
		scn.MoveCardsToTopOfDeck(top4, top3, top2, top1);
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsChooseOption("Discard");
		assertInZone(Zone.DISCARD, top1, top2, top3);
		assertEquals(top4, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(discard + 3, scn.GetFreepsDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoCardsInTheDrawDeckACompanionIsWoundedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var captain = scn.GetShadowCard("captain");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		scn.AttachCardsTo(captain, sword);
		// Leave Sam and Silinde in the draw deck.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("sword"), scn.GetFreepsCard("captain"), scn.GetFreepsCard("lurtz"),
				scn.GetFreepsCard("shagrat"), scn.GetFreepsCard("savage"), scn.GetFreepsCard("overseer"));
		assertEquals(2, scn.GetFreepsDeckCount());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.FreepsResolveSkirmish(aragorn);

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// The discard cannot be carried out in full, so only the wound is left to choose (CR 4.2 "effect").
		assertFalse(scn.FreepsChoiceAvailable("Discard"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, guard, frodo));

		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhereShadowCardsMayNotDiscardFromTheTopOfTheDrawDeckTheDiscardIsStillOfferedAndTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCourteousHalflingScenario();

		var sword = scn.GetShadowCard("sword");
		var captain = scn.GetShadowCard("captain");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("silinde");
		var top3 = scn.GetFreepsCard("guard3");
		// Frodo is Ring-bound; Aragorn and the two Guards are three unbound companions.
		scn.MoveCompanionsToTable(aragorn, guard1, guard2);

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		scn.MoveCardsToTopOfDeck(top3, top2, top1);
		scn.AttachCardsTo(captain, sword);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		// The Shadow cards, Sam, Silinde and the third Guard: more than the three cards the sword would discard.
		assertTrue(deck > 3);

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// Frodo stops Shadow cards that make the Free Peoples player discard, but the Sword gives him a choice
		// and he may choose to discard (F-0205: not a fault): the discard is offered beside the wound, and taken.
		assertTrue(scn.FreepsChoiceAvailable("Wound"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));

		scn.FreepsChooseOption("Discard");
		assertInDiscard(top1, top2, top3);
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(discard + 3, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinOfTheBearerAndNoOtherMinionSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var captain = scn.GetShadowCard("captain");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, sam, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(captain, savage);
		scn.AttachCardsTo(captain, sword);
		// The bearer is made fierce, to win a second skirmish in the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, captain, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, captain}, new PhysicalCardImpl[]{sam, savage});
		int deck = scn.GetFreepsDeckCount();

		// The Savage, 5, beats Sam, 3, and does not bear the sword: nothing to answer, and the next skirmish is chosen.
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsChoiceAvailable("Wound"));
		assertEquals(deck, scn.GetFreepsDeckCount());

		// The Captain, 9 + 2, beats Aragorn.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseOption("Wound");
		scn.FreepsChooseCard(sam);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(deck, scn.GetFreepsDeckCount());

		// And wins again, in the fierce skirmish, killing the Guard.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, captain);
		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, guard);
		scn.FreepsChooseOption("Discard");
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnUrukHaiCultureLurtzBearingItIsDamagePlus1Too() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukHaiLurtzScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// This Lurtz is [uruk-hai], not [isengard], and the text names only the title. He is printed damage +1.
		assertEquals(Culture.URUK_HAI, lurtz.getBlueprint().getCulture());
		assertEquals(1, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, lurtz);
		assertEquals(2, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
