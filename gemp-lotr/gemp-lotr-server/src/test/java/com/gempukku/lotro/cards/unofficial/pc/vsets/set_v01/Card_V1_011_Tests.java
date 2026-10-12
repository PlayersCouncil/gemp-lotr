package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_011_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("library", "101_11");

					// Celeborn, Lord of Lorien: [elven] ally, home site 6, vitality 3.  His fellowship ability is never
					// used here.
					put("celeborn", "1_34");
					// Saelbeth, Elven Councilor: [elven] ally, home site 3, vitality 3.  His skirmish ability is never
					// used here.
					put("saelbeth", "3_25");
					// Bounder: [shire] ally, vitality 2.  His skirmish ability is never used here.
					put("bounder", "1_286");
					put("lorienElf", "1_53");    // Lorien Elf: [elven] companion, vitality 2, no text in play
					// Aragorn, Ranger of the North: [gondor] companion, vitality 4.  His maneuver ability is never
					// used here.
					put("aragorn", "1_89");

					// Tales.  None of them does anything in the fellowship or regroup phase.
					put("bowmen", "3_41");       // Gondor Bowmen: [gondor] condition, support area
					put("stairs", "1_24");       // Stairs of Khazad-dum: [dwarven] condition, support area
					// Red Book of Westmarch: [shire] possession, support area.  Its trigger is to a tale being played,
					// and nothing is played while it is in play.
					put("redbook", "2_113");
					// Artifacts.
					put("coat", "2_105");        // Mithril-coat: [shire], borne by Frodo
					put("staff", "2_22");        // Gandalf's Staff: [gandalf]; only ever in hand or stacked here
					put("scroll", "9_36");       // Scroll of Isildur: [gondor], and a tale as well; only ever stacked here
					// The Shards of Narsil: [gondor], support area.  Another card that cards are stacked on.
					put("shards", "3_44");

					// Lord of Moria: [dwarven] condition, support area, not a tale.  "Each Dwarf is damage +1", and
					// there is no Dwarf.
					put("lordOfMoria1", "1_21");
					put("lordOfMoria2", "1_21");
					// Hobbit Sword: [shire] possession, strength +2 and no text.
					put("sword1", "1_299");
					put("sword2", "1_299");
					// The Palantir of Orthanc, Seventh Seeing-stone: a Shadow [isengard] artifact, support area.  Its
					// ability is a Shadow-phase one that is never used here.
					put("palantir", "4_166");

					put("troop1", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("troop2", "1_177");
					put("savage", "1_151");      // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 and no other text
					put("runner", "1_178");      // Goblin Runner: [moria] Orc, vitality 1, so it can never exert
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LibraryOfRivendellStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Library of Rivendell
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: Fellowship: Stack your tale or artifact from play or from hand here to draw a card.
		 * Regroup: Exert an [elven] ally to draw X cards, exert X minions, and remove up to X wounds from
		 * companions, where X is the number of Free Peoples cultures on cards stacked here.  Discard this artifact.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("library");

		assertEquals("Library of Rivendell", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipActionStacksATaleOrAnArtifactFromPlayOrFromHandAndDrawsACardEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var frodo = scn.GetRingBearer();
		var bowmen = scn.GetFreepsCard("bowmen");
		var coat = scn.GetFreepsCard("coat");
		var redbook = scn.GetFreepsCard("redbook");
		var staff = scn.GetFreepsCard("staff");
		// A tale and an artifact in play, a tale and an artifact in hand.
		scn.MoveCardsToSupportArea(library, bowmen);
		scn.AttachCardsTo(frodo, coat);
		scn.MoveCardsToHand(redbook, staff);

		scn.StartGame();

		// The four cards that will be drawn: none of them is a tale or an artifact, so the hand never holds
		// anything to stack but the two put there.
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"),
				scn.GetFreepsCard("savage"), scn.GetFreepsCard("runner"));
		var deck = scn.GetFreepsDeckCount();

		assertEquals(0, scn.GetStackedCards(library).size());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, bowmen);
		assertTrue(scn.FreepsActionAvailable("Stack your tale or artifact from play"));
		assertTrue(scn.FreepsActionAvailable("Stack your tale or artifact from hand"));

		// A tale from play.
		scn.FreepsChooseAction("Stack your tale or artifact from play");
		assertTrue(scn.FreepsHasCardChoiceAvailable(bowmen, coat));
		scn.FreepsChooseCard(bowmen);

		assertInZone(Zone.STACKED, bowmen);
		assertTrue(scn.GetStackedCards(library).contains(bowmen));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertAttachedTo(coat, frodo);

		// An artifact from play.  The Mithril-coat is now the only one, so it is taken without asking.
		scn.FreepsChooseAction("Stack your tale or artifact from play");

		assertInZone(Zone.STACKED, coat);
		assertTrue(scn.GetStackedCards(library).contains(coat));
		assertNotAttachedTo(coat, frodo);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());

		// A tale from hand: one card leaves the hand and one is drawn.
		assertInZone(Zone.HAND, redbook, staff);
		scn.FreepsChooseAction("Stack your tale or artifact from hand");
		assertTrue(scn.FreepsHasCardChoiceAvailable(redbook, staff));
		scn.FreepsChooseCard(redbook);

		assertInZone(Zone.STACKED, redbook);
		assertTrue(scn.GetStackedCards(library).contains(redbook));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());

		// An artifact from hand, again the only one left.
		scn.FreepsChooseAction("Stack your tale or artifact from hand");

		assertInZone(Zone.STACKED, staff);
		assertEquals(4, scn.GetStackedCards(library).size());
		assertTrue(scn.GetStackedCards(library).contains(staff));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());

		// Nothing is left to stack, from play or from hand.
		assertFalse(scn.FreepsActionAvailable("Stack your tale or artifact"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnlyMyOwnTalesAndArtifactsAreOfferedForStacking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var frodo = scn.GetRingBearer();
		var stairs = scn.GetFreepsCard("stairs");
		var coat = scn.GetFreepsCard("coat");
		var redbook = scn.GetFreepsCard("redbook");
		var staff = scn.GetFreepsCard("staff");
		var lordOfMoria1 = scn.GetFreepsCard("lordOfMoria1");
		var lordOfMoria2 = scn.GetFreepsCard("lordOfMoria2");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var palantir = scn.GetShadowCard("palantir");
		// In play: a condition that is a tale and one that is not, both [dwarven]; an artifact and a possession,
		// both [shire] and both on Frodo; and an artifact that is the opponent's.
		scn.MoveCardsToSupportArea(library, stairs, lordOfMoria1, palantir);
		scn.AttachCardsTo(frodo, coat, sword1);
		// In hand: a possession that is a tale and one that is not; an artifact; a condition that is not a tale.
		scn.MoveCardsToHand(redbook, staff, sword2, lordOfMoria2);

		scn.StartGame();

		// The card that will be drawn is no tale and no artifact.
		var drawn = scn.GetFreepsCard("troop1");
		scn.MoveCardsToTopOfDeck(drawn);

		scn.FreepsChooseAction("Stack your tale or artifact from play");
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, coat));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lordOfMoria1, sword1, palantir));
		// The Library is itself my artifact in play, and cannot be stacked on itself.  The One Ring is no artifact.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(library, scn.GetRing(), frodo));
		scn.FreepsChooseCard(stairs);

		assertInZone(Zone.STACKED, stairs);
		assertInZone(Zone.HAND, drawn);

		scn.FreepsChooseAction("Stack your tale or artifact from hand");
		assertTrue(scn.FreepsHasCardChoiceAvailable(redbook, staff));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword2, lordOfMoria2, drawn));
		scn.FreepsChooseCard(staff);

		assertInZone(Zone.STACKED, staff);
		assertEquals(2, scn.GetStackedCards(library).size());
		assertAttachedTo(coat, frodo);
		assertInZone(Zone.HAND, redbook);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedUntilThereIsATaleOrAnArtifactToStack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var frodo = scn.GetRingBearer();
		var celeborn = scn.GetFreepsCard("celeborn");
		var redbook = scn.GetFreepsCard("redbook");
		var lordOfMoria1 = scn.GetFreepsCard("lordOfMoria1");
		var lordOfMoria2 = scn.GetFreepsCard("lordOfMoria2");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var palantir = scn.GetShadowCard("palantir");
		// Everything that is nearly a tale or an artifact of mine, in play and in hand, and nothing that is one
		// but the Library itself.
		scn.MoveCardsToSupportArea(library, lordOfMoria1, palantir);
		scn.AttachCardsTo(frodo, sword1);
		scn.MoveCardsToHand(sword2, lordOfMoria2);
		// An [elven] ally who could exert, were this the regroup phase.
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();

		// Neither way of stacking is offered, and nor is the regroup ability.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable("Stack your tale or artifact"));
		assertFalse(scn.FreepsActionAvailable(library));

		// A tale arrives in hand.  The game only works out what is on offer when it next asks, so a condition is
		// played to make it ask again.
		scn.MoveCardsToHand(redbook);
		scn.FreepsPlayCard(lordOfMoria2);

		assertTrue(scn.FreepsActionAvailable("Stack your tale or artifact from hand"));
		assertFalse(scn.FreepsActionAvailable("Stack your tale or artifact from play"));
		// The Library's one offer is the stacking: the regroup ability is not among the fellowship actions.
		assertTrue(scn.FreepsActionAvailable(library));
		assertFalse(scn.FreepsActionAvailable("Use Library of Rivendell"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowArtifactInMyHandCanBeStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var redbook = scn.GetFreepsCard("redbook");
		// My own copy of the Shadow artifact.
		var palantir = scn.GetFreepsCard("palantir");
		scn.MoveCardsToSupportArea(library);
		scn.MoveCardsToHand(redbook, palantir);

		scn.StartGame();

		var deck = scn.GetFreepsDeckCount();

		scn.FreepsChooseAction("Stack your tale or artifact from hand");
		assertTrue(scn.FreepsHasCardChoiceAvailable(redbook, palantir));
		scn.FreepsChooseCard(palantir);

		assertInZone(Zone.STACKED, palantir);
		assertTrue(scn.GetStackedCards(library).contains(palantir));
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionExertsAnElvenAllyThenDrawsExertsMinionsAndHealsOnceForEachCultureStackedAndIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var lorienElf = scn.GetFreepsCard("lorienElf");
		var celeborn = scn.GetFreepsCard("celeborn");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var bounder = scn.GetFreepsCard("bounder");
		var bowmen = scn.GetFreepsCard("bowmen");
		var scroll = scn.GetFreepsCard("scroll");
		var redbook = scn.GetFreepsCard("redbook");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, lorienElf);
		scn.MoveAlliesToTable(celeborn, saelbeth, bounder);
		scn.MoveCardsToSupportArea(library);
		// Three cards of two cultures: two [gondor] and one [shire].  X is 2.
		scn.StackCardsOn(library, bowmen, scroll, redbook);
		// Three wounds on companions, and one on an ally.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(saelbeth, 1);

		scn.StartGame();

		scn.MoveMinionsToTable(troop1, troop2, savage);

		// In the maneuver phase the Library offers nothing.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(library));

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		var deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(library));

		scn.FreepsUseCardAction(library);

		// The cost: either [elven] ally, but not the [elven] companion or the [shire] ally, who could both exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(celeborn, saelbeth));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorienElf, bounder, aragorn, frodo));
		scn.FreepsChooseCard(celeborn);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(saelbeth));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());

		// Exactly two of the three minions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, lorienElf, frodo, saelbeth, bounder));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(troop1, savage);

		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(1, scn.GetWoundsOn(savage));

		// Two wounds come off companions.  The wounded allies, the exerted minions and the unwounded companion
		// are not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, saelbeth, lorienElf, troop1, savage));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		scn.FreepsChooseCard(frodo);

		// The third wound stays where it was, and so do the allies' wounds.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(saelbeth));
		assertEquals(2, scn.GetFreepsHandCount());

		// The Library is discarded, and what was stacked on it goes with it.
		assertInDiscard(library, bowmen, scroll, redbook);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesCultureStackedHereTheAllyExertsAndTheLibraryIsDiscardedAndNothingElseHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var shards = scn.GetFreepsCard("shards");
		var coat = scn.GetFreepsCard("coat");
		// My own copy of the Shadow artifact.
		var palantir = scn.GetFreepsCard("palantir");
		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(library, shards);
		// Three cultures that do not count: [isengard] on a Shadow card stacked here, [shire] on a card stacked
		// on another artifact, and the Library's own [elven].  [gondor] and [shire] are in play besides.
		scn.StackCardsOn(library, palantir);
		scn.StackCardsOn(shards, coat);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();

		scn.MoveMinionsToTable(troop1, savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		var deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertTrue(scn.FreepsActionAvailable(library));

		// Celeborn is the only [elven] ally, so he exerts without a choice.
		scn.FreepsUseCardAction(library);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(library, palantir);
		assertInZone(Zone.SUPPORT, shards);
		assertTrue(scn.GetStackedCards(shards).contains(coat));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TwoCardsOfOneCultureStackedInTheFellowshipPhaseCountOnceAndTheOneWoundNeedNotBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var redbook = scn.GetFreepsCard("redbook");
		var coat = scn.GetFreepsCard("coat");
		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(library);
		scn.AttachCardsTo(frodo, coat);
		scn.MoveCardsToHand(redbook);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();

		// The two cards the stacking will draw: my copies of two minions, which can do nothing on my own turn.
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("troop2"), scn.GetFreepsCard("runner"));

		// Two [shire] cards are stacked by the Library's own action, the only tale in hand and the only artifact
		// in play.  X is 1.
		assertEquals(0, scn.GetStackedCards(library).size());
		scn.FreepsChooseAction("Stack your tale or artifact from hand");
		scn.FreepsChooseAction("Stack your tale or artifact from play");
		assertEquals(2, scn.GetStackedCards(library).size());
		assertInZone(Zone.STACKED, redbook, coat);

		scn.MoveMinionsToTable(troop1, savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		var deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(celeborn));

		scn.FreepsUseCardAction(library);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());

		// One of the two minions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(savage);

		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(savage));

		// "Up to" one wound: none is taken off.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(library, redbook, coat);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RemovesNoMoreWoundsThanCulturesAndBothMayComeOffOneCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var bowmen = scn.GetFreepsCard("bowmen");
		var redbook = scn.GetFreepsCard("redbook");
		var troop1 = scn.GetShadowCard("troop1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(library);
		// [gondor] and [shire].  X is 2.
		scn.StackCardsOn(library, bowmen, redbook);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();

		// Two minions, of which only the Troop can exert: the Runner has 1 vitality.
		scn.MoveMinionsToTable(troop1, runner);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(troop1));

		scn.FreepsUseCardAction(library);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(2, scn.GetFreepsHandCount());
		// "Exert 2 minions" with one minion able to exert: that one exerts once, not twice.
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		// The text counts wounds, not companions: both come off Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		scn.FreepsChooseCard(aragorn);

		// Two wounds and no third.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(library);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CulturesStackedHereAreCountedWhetherOrNotACardOfThatCultureIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var celeborn = scn.GetFreepsCard("celeborn");
		var staff = scn.GetFreepsCard("staff");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(library);
		// [gandalf] and [dwarven].  X is 2.  In play are Frodo, Celeborn and the Library: [shire] and [elven] only.
		scn.StackCardsOn(library, staff, stairs);

		scn.StartGame();

		// No minion is in play, so the regroup phase follows the Shadow phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		var deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(celeborn));

		scn.FreepsUseCardAction(library);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertInDiscard(library, staff, stairs);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhenNoElvenAllyCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var lorienElf = scn.GetFreepsCard("lorienElf");
		var celeborn = scn.GetFreepsCard("celeborn");
		var bounder = scn.GetFreepsCard("bounder");
		var bowmen = scn.GetFreepsCard("bowmen");
		var redbook = scn.GetFreepsCard("redbook");
		// An [elven] companion and a [shire] ally who can both exert, and an [elven] ally who cannot: Celeborn
		// has 3 vitality and 2 wounds.
		scn.MoveCompanionsToTable(lorienElf);
		scn.MoveAlliesToTable(celeborn, bounder);
		scn.AddWoundsToChar(celeborn, 2);
		scn.MoveCardsToSupportArea(library);
		scn.StackCardsOn(library, bowmen);
		// A tale in hand, so there is something for the fellowship action to stack.
		scn.MoveCardsToHand(redbook);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable("Stack your tale or artifact from hand"));

		// No minion is in play, so the regroup phase follows the Shadow phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.HAND, redbook);
		assertEquals(0, scn.GetWoundsOn(lorienElf));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(2, scn.GetWoundsOn(celeborn));
		// The regroup ability is not offered, and the tale in hand cannot be stacked in this phase.
		assertFalse(scn.FreepsActionAvailable(library));
		assertFalse(scn.FreepsActionAvailable("Stack your tale or artifact"));
	}
}
