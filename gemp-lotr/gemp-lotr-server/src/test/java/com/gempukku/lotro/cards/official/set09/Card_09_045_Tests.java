package com.gempukku.lotro.cards.official.set09;

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

public class Card_09_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horn", "9_45");
					put("merry", "1_302");      // Merry, Friend to Sam: vitality 4, Frodo signet; a skirmish ability only
					put("pippin", "1_306");     // Pippin, Friend to Frodo: vitality 4, Frodo signet; his text is about [shire] tales
					// Théoden, Tall and Proud: vitality 3, Aragorn signet; valiant, and a trigger when he is killed
					put("theoden", "8_92");
					// I Am Here, cost 3: "Fellowship: Exert Théoden twice to heal every other companion with the
					// Théoden signet twice."
					put("iamhere", "5_84");
					// Merry, Master Holbytla: a [rohan] Merry; enduring and valiant, a Man only beside Eowyn or 3
					// [rohan] companions; his trigger answers another [rohan] Man exerting, and none does here
					put("rohanmerry", "103_85");
					// Pippin, Trollslayer: a [gondor] Pippin; to play, spot a [gondor] companion (cheated in); his
					// skirmish ability is used only on purpose
					put("gondorpippin", "103_31");
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit who is neither Merry nor Pippin

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					// Uruk Seeker: [isengard] Uruk-hai, strength 6, vitality 2, fierce; stronger only against a bearer
					// of a search card
					put("seeker", "4_195");
					// Morgul Hound: [wraith] Orc, strength 5, vitality 2; stronger only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornoftheMarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Horn of the Mark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Strength: 1
		 * Game Text: Bearer must be Merry or Pippin.
		 * 	Bearer gains the Théoden signet.
		 * 	<b>Response:</b> If bearer wins a skirmish, exert him to discard each minion involved in that skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horn");

		assertEquals("Horn of the Mark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerHasTheTheodenSignetForACardThatReadsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Frodo, Son of Drogo: "Fellowship: Exert another companion who has the Frodo signet to heal Frodo."
		var frodo = scn.GetRingBearer();
		var horn = scn.GetFreepsCard("horn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var theoden = scn.GetFreepsCard("theoden");
		var iamhere = scn.GetFreepsCard("iamhere");
		scn.MoveCompanionsToTable(merry, pippin, theoden);
		scn.MoveCardsToHand(horn, iamhere);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(frodo));

		assertTrue(scn.FreepsPlayAvailable(horn));
		scn.FreepsPlayCard(horn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, theoden));
		scn.FreepsChooseCard(merry);
		assertAttachedTo(horn, merry);

		// "Gains": the signet printed on him is still his.  Frodo's ability, which reads the Frodo signet, takes
		// either Hobbit, and not Théoden, who is unwounded here and could exert (his signet is Aragorn's).
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsActionAvailable(frodo));
		scn.FreepsUseCardAction(frodo);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theoden));
		scn.FreepsChooseCard(merry);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Neither Hobbit has the Théoden signet in print.  The one bearing the Horn is healed twice by a card
		// that heals companions with that signet; the other, with the same printed signet, is not.
		assertTrue(scn.FreepsPlayAvailable(iamhere));
		scn.FreepsPlayCard(iamhere);
		assertEquals(2, scn.GetWoundsOn(theoden));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnAMerryOrAPippinOfAnyCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var horn = scn.GetFreepsCard("horn");
		var merry = scn.GetFreepsCard("rohanmerry");
		var pippin = scn.GetFreepsCard("gondorpippin");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(merry, pippin, sam);
		scn.MoveCardsToHand(horn);

		scn.StartGame();
		assertInZone(Zone.HAND, horn);

		// The [rohan] Merry and the [gondor] Pippin; not the [shire] Hobbits of other titles.
		assertTrue(scn.FreepsPlayAvailable(horn));
		scn.FreepsPlayCard(horn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);
		assertAttachedTo(horn, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenBearerWinsHeExertsToDiscardEachMinionOfThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.AttachCardsTo(merry, horn);

		var troop = scn.GetShadowCard("troop");
		var seeker = scn.GetShadowCard("seeker");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, seeker, hound);
		assertAttachedTo(horn, merry);
		// Strengths that only arrange the board: Merry beats the two minions with him, under double their 19;
		// Pippin beats the Hound, under double its 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, merry, null, 16));
		scn.ApplyAdHocModifier(new StrengthModifier(null, pippin, null, 5));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, troop}, new PhysicalCardImpl[]{pippin, hound});
		scn.ShadowAssignToMinions(merry, seeker);

		// Pippin, who does not bear the Horn, wins first: nothing is offered.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(8, scn.GetStrength(pippin));
		assertEquals(5, scn.GetStrength(hound));
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(horn));
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertEquals(1, scn.GetWoundsOn(hound));

		scn.FreepsResolveSkirmish(merry);
		// 3 + 1 for the Horn + 16
		assertEquals(20, scn.GetStrength(merry));
		assertEquals(19, scn.GetStrength(troop) + scn.GetStrength(seeker));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(horn));
		scn.FreepsChooseOptionalTrigger(horn);

		// Both minions of his skirmish are discarded, not killed; the Hound, beaten in another skirmish, stays.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertInZone(Zone.DISCARD, troop, seeker);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);

		// A response with a cost may be used again on the same win while he can still exert (CR 4.2
		// "response"; pitfalls, "A response is offered once per use"): declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(horn));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBearerLosesNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, horn);

		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		assertAttachedTo(horn, merry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, hound);
		scn.FreepsResolveSkirmish(merry);
		// 3 + 1 for the Horn against 5: beaten, and not overwhelmed.
		assertEquals(4, scn.GetStrength(merry));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(merry));
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedBearerWhoWinsIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, horn);
		// Vitality 4: three wounds exhaust him.
		scn.AddWoundsToChar(merry, 3);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertAttachedTo(horn, merry);
		assertEquals(3, scn.GetWoundsOn(merry));
		// A strength that only arranges the board: Merry beats the Troop, under double its 13.
		scn.ApplyAdHocModifier(new StrengthModifier(null, merry, null, 10));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(14, scn.GetStrength(merry));
		assertEquals(13, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		// He won and cannot exert: the Troop is beaten and wounded, and stays.
		assertEquals(3, scn.GetWoundsOn(merry));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
