package com.gempukku.lotro.cards.unofficial.pc.errata.set05;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_085_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swift", "55_85");
					put("guard1", "5_83");      // Household Guard: [rohan] valiant Man companion, no text
					put("guard2", "5_83");
					put("elf", "68_9");         // Elven Defender: [elven] valiant Elf companion; strength +2 only with an [elven] token
					put("rohirrim", "7_246");   // Rohirrim Guard: [rohan] Man companion who is not valiant; a skirmish ability used only on purpose
					put("lord", "1_21");        // Lord of Moria: Free Peoples condition, support area; each Dwarf is damage +1
					put("chasm", "13_44");      // Chasm's Edge: Shadow condition, support area; pumps Gollum only
					put("wielding", "11_105");  // Wielding the Ring: Shadow condition borne by a companion, resistance -2
					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion; bearer exerts when assigned
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LetUsBeSwiftStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Let Us Be Swift
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot 3 valiant companions to discard up to 2 conditions and liberate a site.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swift");

		assertEquals("Let Us Be Swift", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ThreeValiantCompanionsDiscardUpToTwoConditionsAndLiberateTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swift = scn.GetFreepsCard("swift");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var elf = scn.GetFreepsCard("elf");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var lord = scn.GetFreepsCard("lord");
		var chasm = scn.GetShadowCard("chasm");
		var wielding = scn.GetShadowCard("wielding");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(guard1, guard2, elf, rohirrim);
		scn.MoveCardsToHand(swift);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite(site1);
		scn.FreepsTakeControlOfSite(site2);
		scn.ShadowTakeControlOfSite(site3);
		scn.MoveCardsToSupportArea(lord, chasm);
		scn.AttachCardsTo(guard1, wielding);
		scn.AttachCardsTo(rohirrim, poison);
		// No minions: the fellowship moves to site 5 and the game goes straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site2));
		assertTrue(scn.ShadowControls(site3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// Two [rohan] Men and an Elf: the Elf counts, as a valiant companion who is no Man.
		assertTrue(scn.FreepsPlayAvailable(swift));

		scn.FreepsPlayCard(swift);
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(lord, chasm, wielding));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(poison));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(lord, wielding);

		assertInDiscard(swift, lord, wielding);
		assertInZone(Zone.SUPPORT, chasm);
		assertAttachedTo(poison, rohirrim);
		// Site 3 is the highest-numbered site an opponent controls: back on the adventure path.
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertFalse(scn.IsSiteControlled(site3));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWithTwoValiantCompanionsAndARohanCompanionWhoIsNotValiant() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swift = scn.GetFreepsCard("swift");
		var guard1 = scn.GetFreepsCard("guard1");
		var elf = scn.GetFreepsCard("elf");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var chasm = scn.GetShadowCard("chasm");
		scn.MoveCompanionsToTable(guard1, elf, rohirrim);
		scn.MoveCardsToHand(swift);

		scn.StartGame();
		scn.SkipToSite(4);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite(site3);
		scn.MoveCardsToSupportArea(chasm);
		scn.SkipToPhase(Phase.REGROUP);

		// A condition to discard and a site to liberate, and Rohirrim Guard is a [rohan] Man companion.
		assertTrue(scn.ShadowControls(site3));
		assertInZone(Zone.SUPPORT, chasm);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swift));
	}

	@Test
	public void ChoosingNoConditionsDiscardsNothingAndTheSiteIsStillLiberated() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swift = scn.GetFreepsCard("swift");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var elf = scn.GetFreepsCard("elf");
		var lord = scn.GetFreepsCard("lord");
		var chasm = scn.GetShadowCard("chasm");
		scn.MoveCompanionsToTable(guard1, guard2, elf);
		scn.MoveCardsToHand(swift);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveCardsToSupportArea(lord, chasm);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPlayCard(swift);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lord, chasm));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards();

		assertInDiscard(swift);
		assertInZone(Zone.SUPPORT, lord, chasm);
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void PlayableWithNoSiteToLiberateAndDiscardsTheConditionChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swift = scn.GetFreepsCard("swift");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var elf = scn.GetFreepsCard("elf");
		var lord = scn.GetFreepsCard("lord");
		var chasm = scn.GetShadowCard("chasm");
		scn.MoveCompanionsToTable(guard1, guard2, elf);
		scn.MoveCardsToHand(swift);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(lord, chasm);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// An event is played whatever its effect will reach.
		assertTrue(scn.FreepsPlayAvailable(swift));

		scn.FreepsPlayCard(swift);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lord, chasm));
		scn.FreepsChooseCards(chasm);

		assertInDiscard(swift, chasm);
		assertInZone(Zone.SUPPORT, lord);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
