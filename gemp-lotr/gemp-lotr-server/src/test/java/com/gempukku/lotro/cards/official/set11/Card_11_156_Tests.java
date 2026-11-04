package com.gempukku.lotro.cards.official.set11;

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

public class Card_11_156_Tests
{

	// The Fellowship path: site 1, Bag End 1_319, is no plains; site 2, Ettenmoors 1_331, is a plains (its skirmish
	// ability is never used).  Edoras Hall 7_330 (each [rohan] possession costs 1 less) is not under the play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mount", "11_156");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5, vitality 3; a skirmish ability used only on purpose
					put("rider2", "4_286");     // a second Rider of Rohan (not unique), with no mount: the [rohan] Man companion who is not the bearer
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally; an archery ability that needs a villager
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted); its play trigger is never met
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Westemnet Hills 4_344 as site 4: a plains, and the home of Guma (4T).  Site 3, the home
	// of the Rohirrim Herdsman (3T), is Barrows of Edoras 4_337, a sanctuary and no plains.  Moving to Westemnet
	// Hills heals each mounted ally; nobody here is wounded.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mount", "11_156");
					put("mount2", "11_156");
					put("mount3", "11_156");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion; valiant and nothing else
					put("guma", "4_277");       // Guma: [rohan] Man ally, home 4T; a fellowship ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T; a regroup ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site4", "4_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimMountStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Rohirrim Mount
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a [rohan] Man.<br>At the start of each skirmish involving bearer, each minion skirmishing bearer must exert.<br>While bearer is at a plains site, he or she is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mount");

		assertEquals("Rohirrim Mount", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BorneByAnAllyItIsDefenderPlus1OnlyWhileHisHomeSiteIsAPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var mount3 = scn.GetFreepsCard("mount3");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma, herdsman);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(guma, mount2);
		scn.AttachCardsTo(herdsman, mount3);

		scn.StartGame();
		scn.SkipToSite(4);
		assertEquals("Westemnet Hills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetSite(3), Keyword.PLAINS));

		// The companion is with the fellowship, and Guma at his home site, the plains it is at: both defender +1.
		// The Herdsman is at his home site, Barrows of Edoras, which is no plains (CR 4.2 "ally").
		assertTrue(scn.HasKeyword(guard, Keyword.DEFENDER));
		assertTrue(scn.HasKeyword(guma, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(herdsman, Keyword.DEFENDER));
	}

	@Test
	public void IsPlayedOnARohanManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(rider, boromir, merry);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(mount);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(mount));
		scn.FreepsPlayCard(mount);
		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, nor Merry, a [rohan] Hobbit who is no
		// Man beside two [rohan] companions and no Eowyn, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(rider);
		assertAttachedTo(mount, rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfBearersSkirmishEachMinionSkirmishingHimExertsAndNoOtherSkirmishDoesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(rider, rider2);
		scn.AttachCardsTo(rider, mount);
		assertAttachedTo(mount, rider);

		scn.StartGame();
		// The second Rider is given the strength to live through the Troop: 5 + 4 against 13, which is under double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, rider2, null, 4));
		scn.MoveMinionsToTable(troop1, savage, runner, troop2, troop3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, troop1}, new PhysicalCardImpl[]{rider2, troop2}, new PhysicalCardImpl[]{frodo, troop3});
		// The Uruk Savage and the Goblin Runner were left over: the Shadow player puts them on the Rider as well.
		scn.ShadowAssignToMinions(rider, savage, runner);

		// The second Rider's skirmish first, a [rohan] Man with no mount: nobody exerts, those assigned to the bearer included.
		scn.FreepsResolveSkirmish(rider2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));

		scn.PassSkirmishActions();
		// The second Rider, 9 against 13, loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(rider2));
		assertEquals(0, scn.GetWoundsOn(troop2));

		// The bearer's skirmish: an Orc and an Uruk-hai, each exerts once; the Runner, with 1 vitality, cannot exert
		// and is neither wounded nor killed.  The Troop that fought the second Rider does not exert, nor the one
		// assigned to Frodo, whose skirmish has not begun.
		assertEquals(0, scn.GetWoundsOn(runner));
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));
	}

	@Test
	public void AMinionSkirmishingBearerAgainInTheFierceRoundExertsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);
		assertAttachedTo(mount, rider);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, pursuer);

		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(pursuer));

		// 5 against 5: a tie goes to the Shadow side, so the Rider takes a wound and the Pursuer none.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(pursuer));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(rider, pursuer);
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetWoundsOn(pursuer));
	}

	@Test
	public void AtAPlainsBearerIsDefenderPlus1AndAnotherRohanManIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		scn.MoveCompanionsToTable(rider, rider2);
		scn.AttachCardsTo(rider, mount);
		assertAttachedTo(mount, rider);

		scn.StartGame();

		// Bag End, site 1: no plains.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(rider2, Keyword.DEFENDER));

		// The move to Ettenmoors, site 2, a plains: the bearer is defender +1, and the Rider with no mount is not.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(1, scn.GetKeywordCount(rider, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(rider2, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
