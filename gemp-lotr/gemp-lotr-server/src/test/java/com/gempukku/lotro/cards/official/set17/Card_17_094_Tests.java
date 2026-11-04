package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_094_Tests
{

	// The Fellowship path with Westemnet Plains 4_342 as site 3: Bag End (site 1) is no plains, Ettenmoors (site 2)
	// is a plains, and Westemnet Plains is a plains and a sanctuary (its text is about [dunland] Men in archery).
	protected VirtualTableScenario GetPlainsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dispatched", "17_94");
					put("guard", "5_83");        // Household Guard: [rohan] Man companion, strength 6; valiant and no other text
					put("guard2", "5_83");
					put("arwen", "1_30");        // Arwen, Daughter of Elrond: [elven] Elf companion, strength 6; +3 only while skirmishing a Nazgûl
					put("herdsman", "7_247");    // Rohirrim Herdsman: a [rohan] Man who is an ally, strength 4; a regroup ability only
					put("mount", "4_287");       // Rider's Mount: [rohan] mount on a [rohan] Man; minions skirmishing bearer exert
					put("mount2", "4_287");
					put("horse", "4_283");       // Horse of Rohan: [rohan] mount on a Man, Elf or Wizard; minions skirmishing bearer exert
					put("chain", "4_278");       // Heavy Chain: [rohan] armor on a Man, no mount
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "4_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dispatched", "17_94");
					put("guard", "5_83");        // Household Guard: [rohan] Man companion; valiant and no other text
					put("herdsman", "7_247");    // Rohirrim Herdsman: a [rohan] Man who is an ally; his regroup ability discards 3 cards from hand, used only on purpose
					put("merry", "103_85");      // Merry, Master Holbytla: [rohan] Hobbit, no Man beside one other [rohan] companion
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability only

					put("mount", "4_287");       // Rider's Mount: [rohan] possession (mount) on a [rohan] Man
					put("chain", "4_278");       // Heavy Chain: [rohan] possession (armor) on a Man
					put("stronghold", "102_48"); // Stronghold of My Fathers: [rohan] possession in the support area; a response to a wound only
					put("arrow", "9_46");        // The Red Arrow: [rohan] ARTIFACT in the support area; an assignment ability only
					put("sword", "1_112");       // Ranger's Sword: [gondor] possession on Aragorn; damage +1
					put("poison", "8_28");       // Spider Poison: [gollum] Shadow possession in its owner's support area; a skirmish ability about Shelob
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DispatchedWithHasteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Dispatched With Haste
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While at a plains site, each mounted companion is strength +1.<br><b>Regroup:</b> Spot 2 [rohan] Men and discard a [rohan] possession to discard a possession.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dispatched");

		assertEquals("Dispatched With Haste", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtAPlainsSiteEachMountedCompanionIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlainsScenario();

		var frodo = scn.GetRingBearer();
		var dispatched = scn.GetFreepsCard("dispatched");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var arwen = scn.GetFreepsCard("arwen");
		var herdsman = scn.GetFreepsCard("herdsman");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var horse = scn.GetFreepsCard("horse");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(guard, guard2, arwen);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(arwen, horse);
		scn.AttachCardsTo(guard2, chain);
		scn.AttachCardsTo(herdsman, mount2);
		scn.MoveCardsToSupportArea(dispatched);
		assertAttachedTo(mount, guard);
		assertAttachedTo(horse, arwen);
		assertAttachedTo(chain, guard2);
		assertAttachedTo(mount2, herdsman);

		scn.StartGame();

		// Bag End is no plains.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(4, scn.GetStrength(herdsman));
		// 3 + 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));

		// Ettenmoors, a plains: a [rohan] Man on a Rider's Mount and an Elf on a Horse of Rohan are +1; a companion
		// bearing a possession that is no mount, a mounted ally and a companion with no mount are not.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(4, scn.GetStrength(frodo));

		// Westemnet Plains, a plains that is also a sanctuary.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(4, scn.GetStrength(herdsman));
	}

	@Test
	public void RegroupAbilityDiscardsARohanPossessionToDiscardAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dispatched = scn.GetFreepsCard("dispatched");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mount = scn.GetFreepsCard("mount");
		var chain = scn.GetFreepsCard("chain");
		var stronghold = scn.GetFreepsCard("stronghold");
		var arrow = scn.GetFreepsCard("arrow");
		var sword = scn.GetFreepsCard("sword");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, mount, chain);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(dispatched, stronghold, arrow, poison);
		assertAttachedTo(mount, guard);
		assertAttachedTo(chain, guard);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, dispatched, stronghold, arrow, poison);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// The two [rohan] Men are a companion and an ally.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(dispatched));
		scn.FreepsUseCardAction(dispatched);

		// The cost: a [rohan] possession, borne or in the support area; not a [rohan] artifact, a [rohan] condition,
		// a possession of another culture or a Shadow possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(mount, chain, stronghold));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arrow, dispatched, sword, poison));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chain);
		assertInZone(Zone.DISCARD, chain);

		// The target: any possession, of either side; not an artifact or a condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(mount, stronghold, sword, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arrow, dispatched));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(poison);
		assertInZone(Zone.DISCARD, poison);

		assertAttachedTo(mount, guard);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, stronghold, arrow, dispatched);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithOneRohanManToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dispatched = scn.GetFreepsCard("dispatched");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mount = scn.GetFreepsCard("mount");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(dispatched);
		assertAttachedTo(mount, guard);
		assertAttachedTo(sword, aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// The Guard is the one [rohan] Man: Merry is [rohan] and no Man (only two [rohan] companions), Aragorn a Man
		// and no [rohan]. The cost could be paid and there is a possession to discard.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(dispatched));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoRohanPossessionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dispatched = scn.GetFreepsCard("dispatched");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arrow = scn.GetFreepsCard("arrow");
		var sword = scn.GetFreepsCard("sword");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(dispatched, arrow, poison);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, arrow, poison);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// A [rohan] artifact, a [gondor] possession and a Shadow possession, and no [rohan] possession.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(dispatched));
	}

	@Test
	public void WithNoOtherPossessionTheCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dispatched = scn.GetFreepsCard("dispatched");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var chain = scn.GetFreepsCard("chain");
		var arrow = scn.GetFreepsCard("arrow");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, chain);
		scn.MoveCardsToSupportArea(dispatched, arrow);
		assertAttachedTo(chain, guard);
		assertInZone(Zone.SUPPORT, dispatched, arrow);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(dispatched));
		scn.FreepsUseCardAction(dispatched);

		// The one [rohan] possession is paid with nothing asked, and then nothing is left to discard.
		assertInZone(Zone.DISCARD, chain);
		assertInZone(Zone.SUPPORT, arrow, dispatched);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
