package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "18_95");
					// Éomer, Third Marshal of Riddermark: [rohan] Man companion, strength 7, vitality 3; his strength
					// bonus counts wounds on minions he is skirmishing, and he skirmishes nobody here
					put("eomer", "4_267");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, vitality 3; valiant is its whole text
					// Rohirrim Herdsman: a [rohan] Man ally, strength 4; a regroup ability used only on purpose
					put("herdsman", "7_247");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions or Éowyn can be
					// spotted (here, two)
					put("merry", "103_85");
					put("spear", "4_288");      // Rider's Spear: a [rohan] hand weapon, strength +2; damage +1 if mounted
					put("mount", "4_287");      // Rider's Mount: a [rohan] mount, no bonus; a start-of-skirmish trigger only
					put("mount2", "4_287");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1
					put("assassin", "2_93");    // Tower Assassin: [sauron] Orc, vitality 3; a maneuver ability used only on purpose
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, vitality 1, so he cannot exert
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomersBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Éomer's Bow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Game Text: Bearer must be a [rohan] Man.<br>If bearer is mounted, he or she is strength +1.<br><b>Archery:</b> If bearer is Éomer, exert him to choose a Shadow player who must exert one of his or her minions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Éomer's Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARohanManCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		// Two [rohan] companions, the Guard and Merry, and no Éowyn: Merry is no Man.
		scn.MoveCompanionsToTable(guard, aragorn, merry);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		scn.FreepsPlayCard(bow);
		// [rohan] Men, companions and an ally; not a [gondor] Man, not a [rohan] Hobbit, not Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merry, scn.GetRingBearer()));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(herdsman);

		assertAttachedTo(bow, herdsman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionBearerIsStrengthPlus1OnceAMountIsPlayedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var spear = scn.GetFreepsCard("spear");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, bow, spear);
		scn.AttachCardsTo(herdsman, mount2);
		scn.MoveCardsToHand(mount);
		assertAttachedTo(bow, guard);
		assertAttachedTo(spear, guard);
		assertAttachedTo(mount2, herdsman);

		scn.StartGame();
		// Bearing a possession that is no mount: 6 + 2 for the Spear, nothing for the Bow.  The Herdsman is mounted
		// and bears no Bow: his 4.
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(herdsman));

		scn.FreepsPlayCard(mount);
		assertAttachedTo(mount, guard);
		// The Mount has no bonus of its own: 6 + 2 + 1.
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(herdsman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyBearerIsStrengthPlus1WhileMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(herdsman, bow, mount);
		scn.AttachCardsTo(guard, mount2);
		assertAttachedTo(bow, herdsman);
		assertAttachedTo(mount, herdsman);
		assertAttachedTo(mount2, guard);

		scn.StartGame();
		// 4 + 1; the Guard is mounted and bears no Bow: his 6.
		assertEquals(5, scn.GetStrength(herdsman));
		assertEquals(6, scn.GetStrength(guard));
	}

	@Test
	public void ArcheryAbilityExertsEomerToHaveTheShadowPlayerExertOneOfHisMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, bow);
		assertAttachedTo(bow, eomer);

		var savage = scn.GetShadowCard("savage");
		var assassin = scn.GetShadowCard("assassin");
		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, assassin, runner1);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(runner1));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		assertEquals(1, scn.GetWoundsOn(eomer));
		// The Shadow player chooses among his minions that can exert: an [isengard] Uruk-hai and a [sauron] Orc,
		// not the Runner (vitality 1).
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, assassin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner1, eomer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(runner1));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithNoMinionAbleToExertEomerIsExertedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, bow);
		assertAttachedTo(bow, eomer);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(runner1));
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileARohanManWhoIsNotEomerBearsTheBow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(guard, bow);
		assertAttachedTo(bow, guard);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ARCHERY);
		// Éomer is on the table and could exert, and so could the Guard; the bearer is not Éomer.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(guard));
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileEomerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(eomer, bow);
		assertAttachedTo(bow, eomer);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 3, 2 wounds: Éomer cannot exert.  The Guard, who could, is not the bearer.
		scn.AddWoundsToChar(eomer, 2);
		assertEquals(2, scn.GetWoundsOn(eomer));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
		assertEquals(2, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(savage));
	}
}
