package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_007_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("there", "101_7");
					// Arwen, Daughter of Elrond: Elf, strength 6, vitality 3.  Her +3 is only against a Nazgul.
					put("arwen", "1_30");
					// Lorien Elf: Elf, strength 4, vitality 2.  Its only text is what it takes to play it.
					put("lorien", "1_53");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man, strength 8, vitality 4
					// Elrond, Lord of Rivendell: Elf ally, strength 8, whose home is site 3 of this path.  His
					// other text heals allies at the start of a turn and draws a card in the fellowship phase.
					put("elrond", "1_40");
					put("gilgalad", "1_66");    // The Tale of Gil-galad: [elven] tale, borne by an Elf
					// Beren and Luthien: [elven] tale in the support area.  Its ability needs an [elven] ally.
					put("beren", "3_9");
					put("saga", "1_114");       // The Saga of Elendil: [gondor] tale, borne by a [gondor] companion
					// Mallorn-trees: [elven] condition in the support area that is not a tale
					put("mallorn", "1_54");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria], strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard], strength 5, vitality 3, damage +1
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard], strength 9, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IWasThereStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: I Was There
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Tale.
		 * Spot a minion skirmishing an Elf and discard your [elven] tale to wound that minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("there");

		assertEquals("I Was There", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAChosenElvenTaleOfMineToWoundTheMinionSkirmishingAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var there = scn.GetFreepsCard("there");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var beren = scn.GetFreepsCard("beren");
		var saga = scn.GetFreepsCard("saga");
		var mallorn = scn.GetFreepsCard("mallorn");
		var theirBeren = scn.GetShadowCard("beren");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, aragorn);
		// Two [elven] tales of mine, one borne and one in the support area.  Beside them: a tale that is not
		// [elven], an [elven] condition that is not a tale, and an [elven] tale that is the opponent's.
		scn.AttachCardsTo(arwen, gilgalad);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(beren, mallorn, theirBeren);
		scn.MoveCardsToHand(there);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, troop);

		assertInHand(there);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, beren);
		assertTrue(scn.FreepsPlayAvailable(there));

		scn.FreepsPlayCard(there);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gilgalad, beren));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saga, mallorn, theirBeren));

		scn.FreepsChooseCard(beren);
		assertInDiscard(beren);
		assertAttachedTo(gilgalad, arwen);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn, theirBeren);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(there);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WoundsOnlyTheChosenOneOfTheMinionsSkirmishingTheElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var there = scn.GetFreepsCard("there");
		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(arwen, lorien);
		scn.AttachCardsTo(arwen, gilgalad);
		scn.MoveCardsToHand(there);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, savage, party);
		scn.SkipToAssignments();
		// Arwen takes a [moria] minion and the other Elf an [isengard] one; the Shadow player then adds the
		// remaining [isengard] minion to Arwen's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, troop}, new PhysicalCardImpl[]{lorien, party});
		scn.ShadowAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		// Arwen's skirmish is under way.  The Raiding Party is assigned to an Elf too, but that skirmish has
		// not begun, so it is not yet skirmishing anyone.
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, troop));
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, savage));
		assertTrue(scn.IsCharAssignedAgainst(lorien, party));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(party));

		// The only [elven] tale is discarded without asking; the minion is then chosen.
		scn.FreepsPlayCard(there);
		assertInDiscard(gilgalad);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(party));

		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(party));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CanBePlayedInTheSkirmishOfAnElfAllyAtHisHomeSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var there = scn.GetFreepsCard("there");
		var elrond = scn.GetFreepsCard("elrond");
		var beren = scn.GetFreepsCard("beren");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(elrond, beren);

		scn.StartGame();

		// An ally may be assigned to a skirmish while the fellowship is at his home site, so the fellowship
		// moves from site 2 to site 3.
		scn.SkipToSite(2);
		scn.MoveCardsToHand(there);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignAndResolve(elrond, savage);

		assertTrue(scn.IsCharSkirmishingAgainst(elrond, savage));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SUPPORT, beren);
		assertTrue(scn.FreepsPlayAvailable(there));

		scn.FreepsPlayCard(there);
		assertInDiscard(beren, there);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheSkirmishOfACompanionWhoIsNotAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var there = scn.GetFreepsCard("there");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.AttachCardsTo(arwen, gilgalad);
		scn.MoveCardsToHand(there);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{arwen, savage});

		// Aragorn's skirmish: Arwen is assigned to a minion, but neither of them is skirmishing yet.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertTrue(scn.IsCharAssignedAgainst(arwen, savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(there));

		// Aragorn, 8 against 13, loses and takes a wound.
		scn.PassSkirmishActions();

		// Arwen's skirmish, on the same board.
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(there));
		assertInHand(there);
		assertAttachedTo(gilgalad, arwen);
	}

	@Test
	public void CannotBePlayedWithoutAnElvenTaleOfMineInPlayToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var there = scn.GetFreepsCard("there");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var saga = scn.GetFreepsCard("saga");
		var mallorn = scn.GetFreepsCard("mallorn");
		var theirBeren = scn.GetShadowCard("beren");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, aragorn);
		// In play: a tale that is not [elven], an [elven] condition that is not a tale, and the opponent's
		// [elven] tale.  My only [elven] tale is in my hand, and "discard" means from play.
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(mallorn, theirBeren);
		scn.MoveCardsToHand(there, gilgalad);

		scn.StartGame();

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, savage);

		assertTrue(scn.IsCharSkirmishingAgainst(arwen, savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(there));
		assertInHand(there, gilgalad);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn, theirBeren);
	}
}
