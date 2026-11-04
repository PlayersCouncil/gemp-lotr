package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("drawn", "18_19");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; +1 for each race in the fellowship
					// Radagast, The Brown: a [gandalf] Wizard who is an ally, the one printed kind of [gandalf] Wizard that
					// is no companion; his fellowship ability is never used here
					put("radagast", "31_17");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard; +2 only with 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability never used here

					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] possession, hand weapon, strength +2, on a [moria] Orc; a play trigger only
					put("feral", "11_183");     // Feral Uruk: [uruk-hai] Uruk-hai, strength 9; +1 at a battleground, which site 2 is not
					put("spear", "17_117");     // Spear of the White Hand: [uruk-hai] possession, strength +1, vitality +1, no other text
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: [wraith] Nazgul, strength 14, fierce; +2 for each other Nazgul
					put("blade", "1_221");      // The Pale Blade: possession, hand weapon, strength +3, damage +1; its response is not reached
					put("steed", "1_208");      // Black Steed: possession, mount, strength +2, and +2 more at a plains site (Ettenmoors, site 2)
					// Ring of Asperity: [wraith] artifact, a ring, strength +1, on a Nazgul; its play trigger and its
					// reconcile response are not reached
					put("ring", "9_42");
					// Saruman, Coldly Still: a Wizard minion of the [men] culture; his texts are about [men] minions and
					// possessions and are not reached
					put("saruman", "17_48");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion, strength 3, vitality 4; a maneuver ability that needs a
					// site he controls and a regroup ability, neither reached
					put("ted", "18_75");
					// Henchman's Dagger: [men] possession, hand weapon, strength +1, on a [men] minion, borne beside one
					// other hand weapon; no other text
					put("dagger", "18_69");
					put("dagger2", "18_69");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DrawntoFullHeightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Drawn to Full Height
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>.<br>Spot a [gandalf] Wizard to make a minion lose all strength bonuses from possessions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("drawn");

		assertEquals("Drawn to Full Height", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingGandalfAMinionLosesEveryStrengthBonusFromPossessionsForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drawn = scn.GetFreepsCard("drawn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(drawn);
		assertInHand(drawn);

		var scout = scn.GetShadowCard("scout");
		var scimitar = scn.GetShadowCard("scimitar");
		var feral = scn.GetShadowCard("feral");
		var spear = scn.GetShadowCard("spear");
		var witchking = scn.GetShadowCard("witchking");
		var blade = scn.GetShadowCard("blade");
		var steed = scn.GetShadowCard("steed");
		var ring = scn.GetShadowCard("ring");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, feral, witchking);
		scn.AttachCardsTo(scout, scimitar);
		scn.AttachCardsTo(feral, spear);
		scn.AttachCardsTo(witchking, blade, steed, ring);
		assertAttachedTo(scimitar, scout);
		assertAttachedTo(spear, feral);
		assertAttachedTo(blade, witchking);
		assertAttachedTo(steed, witchking);
		assertAttachedTo(ring, witchking);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, scout},
				new PhysicalCardImpl[]{gandalf, feral});
		// The Witch-king is left unassigned.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(scout));        // 6 + 2
		assertEquals(10, scn.GetStrength(feral));       // 9 + 1
		assertEquals(22, scn.GetStrength(witchking));   // 14 + 3 blade + 2 steed + 2 steed at plains + 1 ring
		assertTrue(scn.FreepsPlayAvailable(drawn));
		scn.FreepsPlayCard(drawn);

		// Any minion: the one skirmishing, one assigned and waiting, one not assigned at all; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(scout, feral, witchking));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gandalf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(witchking);

		// Both possessions' bonuses go, the plains bonus of the Steed's own text with them; the artifact's +1 stays.
		assertEquals(15, scn.GetStrength(witchking));   // 14 + 1 ring
		assertEquals(8, scn.GetStrength(scout));
		assertEquals(10, scn.GetStrength(feral));
		assertAttachedTo(blade, witchking);
		assertAttachedTo(steed, witchking);
		assertInDiscard(drawn);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn 8 against the Scout's 8 loses, and lives. In the next skirmish the bonuses are back.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(22, scn.GetStrength(witchking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AGandalfWizardWhoIsAnAllyIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drawn = scn.GetFreepsCard("drawn");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(drawn);
		assertInHand(drawn);

		var scout = scn.GetShadowCard("scout");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.AttachCardsTo(scout, scimitar);
		assertAttachedTo(scimitar, scout);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scout);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(scout));        // 6 + 2
		assertTrue(scn.FreepsPlayAvailable(drawn));
		scn.FreepsPlayCard(drawn);

		// The one minion is chosen without a question.
		assertEquals(6, scn.GetStrength(scout));
		assertInDiscard(drawn);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAGandalfCompanionWhoIsNoWizardAndAWizardOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drawn = scn.GetFreepsCard("drawn");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.MoveCardsToHand(drawn);
		assertInHand(drawn);

		var scout = scn.GetShadowCard("scout");
		var scimitar = scn.GetShadowCard("scimitar");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, saruman);
		scn.AttachCardsTo(scout, scimitar);
		assertAttachedTo(scimitar, scout);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scout);
		// Saruman, a [men] Wizard, stays in play unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertInPlay(saruman);
		assertEquals(8, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(drawn));
		assertInHand(drawn);
	}

	@Test
	public void APossessionPlayedOnTheMinionLaterInTheSkirmishAddsNoStrengthEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drawn = scn.GetFreepsCard("drawn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(drawn);
		assertInHand(drawn);

		var ted = scn.GetShadowCard("ted");
		var dagger = scn.GetShadowCard("dagger");
		var dagger2 = scn.GetShadowCard("dagger2");
		var saruman = scn.GetShadowCard("saruman");
		// The second Dagger waits in the Shadow player's discard pile for Saruman's skirmish ability.
		scn.MoveCardsToDiscard(dagger2);
		assertInDiscard(dagger2);

		scn.StartGame();
		scn.MoveMinionsToTable(ted, saruman);
		scn.AttachCardsTo(ted, dagger);
		assertAttachedTo(dagger, ted);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, ted);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);

		// 3 + 1 Dagger + 2 from Saruman's first sentence (a [men] minion bearing a possession), which is no possession's bonus
		assertEquals(6, scn.GetStrength(ted));
		scn.FreepsPlayCard(drawn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(ted, saruman));
		scn.FreepsChooseCard(ted);
		assertEquals(5, scn.GetStrength(ted));

		// Saruman exerts to play the other Dagger from the discard pile onto Ted: its +1 does not land.
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);
		// The one Dagger in the pile is played; its bearer is chosen from the two [men] minions.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ted, saruman));
		scn.ShadowChooseCard(ted);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertAttachedTo(dagger2, ted);
		assertEquals(5, scn.GetStrength(ted));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
