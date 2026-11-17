package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_038_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  The Archer is played from hand in the Shadow phase of turn 1, at The Troll Hoard 30_50 (site 2); the
	// pool is set high and not read.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("archer", "33_38");
					put("archer2", "33_38");
					put("bowman", "2_60");        // Goblin Bowman: a [moria] Orc archer with no other text, played after the Archer

					// [dwarven] companions.
					put("thorin", "30_19");       // Thorin, Oakenshield: a Fellowship ability, not used
					put("balin", "30_2");         // Balin, Brother of Dwalin: a Fellowship ability, not used
					put("gloin", "30_11");        // Glóin, Father of Gimli: a Skirmish ability, not used
					// A [dwarven] character that is no companion.
					put("dain", "32_2");          // Dain Ironfoot: [dwarven] ally; Thorin +1, and a start-of-regroup offer
					// "Bilbo may not take wounds (except during the archery phase)".
					put("consorting", "2_97");    // Consorting With Wizards: [shire] tale, bearer must be Bilbo
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GoblinArcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Goblin Archer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 4
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.<br>When you play this minion, you may spot 3 [dwarven] companions to add a doubt. The Free Peoples player may wound Bilbo to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("archer");

		assertEquals("Goblin Archer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithThreeDwarvenCompanionsItsPlayMayAddADoubtWhichTheFreePeoplesPlayerDoesNotPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin, gloin);
		scn.MoveCardsToHand(archer);

		scn.StartGame();
		assertInHand(archer);
		scn.MoveAlliesToTable(dain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, dain);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.ShadowPlayCard(archer);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(archer));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayWoundBilboToPreventTheDoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin, gloin);
		scn.MoveCardsToHand(archer);

		scn.StartGame();
		assertInHand(archer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.ShadowPlayCard(archer);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// Bilbo is wounded, no doubt is added, and no other companion is wounded.
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferAddsNothingAndAnotherOrcOrASecondCopyPlayedAfterItGetsNoOfferFromIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var archer2 = scn.GetShadowCard("archer2");
		var bowman = scn.GetShadowCard("bowman");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin, gloin);
		scn.MoveCardsToHand(archer, archer2, bowman);

		scn.StartGame();
		assertInHand(archer, archer2, bowman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(archer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(archer));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [moria] Orc archer played while the Archer is in play: the Archer makes no offer.
		scn.ShadowPlayCard(bowman);
		assertInZone(Zone.SHADOW_CHARACTERS, bowman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(archer));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played: it makes its own offer, and the first copy makes none.
		scn.ShadowPlayCard(archer2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(archer2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(archer));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoDwarvenCompanionsAndADwarvenAllyAreNotEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveCardsToHand(archer);

		scn.StartGame();
		assertInHand(archer);
		scn.MoveAlliesToTable(dain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Two [dwarven] companions, a [dwarven] ally and a [shire] companion (Bilbo): no offer.
		assertInZone(Zone.SUPPORT, dain);
		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(archer);

		assertInZone(Zone.SHADOW_CHARACTERS, archer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(archer));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenBilboCannotTakeWoundsThePreventionIsNotAskedAndTheDoubtIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var consorting = scn.GetFreepsCard("consorting");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin, gloin);
		scn.AttachCardsTo(bilbo, consorting);
		scn.MoveCardsToHand(archer);

		scn.StartGame();
		assertInHand(archer);
		assertAttachedTo(consorting, bilbo);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.ShadowPlayCard(archer);
		scn.ShadowAcceptOptionalTrigger();

		// Bilbo may not take wounds outside the archery phase: the price cannot be paid and is not asked.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
