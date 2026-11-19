package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swordwall", "4_84");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6; +3 only against a Nazgul
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion; +2 only at Fellowship sites 6 to 8
					put("lorienelf", "1_53");   // Lorien Elf: Elf companion with a play requirement only
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an Elf who is no companion (an ally); a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, so it can never exert; a "when you play" only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwordwallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Sword-wall
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish or Regroup
		 * Game Text: <b>Skirmish</b> <i>or</i> <b>Regroup:</b> Spot 3 Elf companions to liberate a site or exert a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swordwall");

		assertEquals("Sword-wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void InASkirmishWithBothOnOfferThePlayerChoosesToExertAMinionInOrOutOfTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordwall = scn.GetFreepsCard("swordwall");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, haldir, lorienelf);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveCardsToHand(swordwall);
		scn.MoveMinionsToTable(troop, savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, troop);
		// The Savage and the Runner are left in no skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.ShadowControls(site1));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Three Elf companions of three titles.
		assertTrue(scn.FreepsPlayAvailable(swordwall));
		scn.FreepsPlayCard(swordwall);

		// A site the opponent controls and minions that can exert: both are on offer.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Exert a minion");

		// A minion of either culture and race, in the skirmish or not; not one that cannot exert, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, arwen, haldir, lorienelf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.ShadowControls(site1));
		assertInDiscard(swordwall);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheRegroupPhaseWithNoMinionItLiberatesTheHighestSiteAnOpponentControlsUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordwall = scn.GetFreepsCard("swordwall");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// The opponent controls sites 1 and 2; the player himself the highest, site 3.
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.FreepsTakeControlOfSite(site3);
		scn.MoveCompanionsToTable(arwen, haldir, lorienelf);
		scn.MoveCardsToHand(swordwall);
		// No minion in play: the game goes from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));
		assertInHand(swordwall);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(swordwall));
		scn.FreepsPlayCard(swordwall);

		// No minion to exert: the liberation is what can be done, and nothing is asked.  The highest site an
		// opponent controls goes back on the path, passing over the player's own (CR 4.2 "liberate").
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		assertInDiscard(swordwall);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsTheMinionIsExertedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordwall = scn.GetFreepsCard("swordwall");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		scn.MoveCompanionsToTable(arwen, haldir, lorienelf);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// The only controlled site is the player's own, which cannot be liberated.
		scn.FreepsTakeControlOfSite(site1);
		scn.MoveCardsToHand(swordwall);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(swordwall));
		scn.FreepsPlayCard(swordwall);

		// No choice between the two: the minion to exert is asked at once.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsControls(site1));
		assertInDiscard(swordwall);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNeitherASiteToLiberateNorAMinionThatCanExertItIsStillPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordwall = scn.GetFreepsCard("swordwall");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		scn.MoveCompanionsToTable(arwen, haldir, lorienelf);
		scn.MoveCardsToHand(swordwall);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		// Nobody controls a site, and the only minion has vitality 1.  An event whose effect finds nothing
		// is still played (F-0665).
		assertFalse(scn.IsSiteControlled(scn.GetSite(1)));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(swordwall));
		scn.FreepsPlayCard(swordwall);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInDiscard(swordwall);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithTwoElfCompanionsBesideAnElfAllyAndACompanionWhoIsNoElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordwall = scn.GetFreepsCard("swordwall");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, haldir, aragorn);
		scn.MoveAlliesToTable(celeborn);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveCardsToHand(swordwall);
		assertInHand(swordwall);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);

		// A site to liberate and a minion to exert, but only two Elf companions.
		assertTrue(scn.ShadowControls(site1));
		assertInHand(swordwall);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swordwall));
	}
}
