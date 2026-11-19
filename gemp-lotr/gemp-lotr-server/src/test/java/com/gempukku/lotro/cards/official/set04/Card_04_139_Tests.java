package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_139_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banished", "4_139");
					// Gríma, Wormtongue: an [isengard] Man, vitality 3; a maneuver ability never used.
					put("grima", "4_154");
					// Rohirrim Traitor: an [isengard] Man, vitality 2; strength +2 while a companion bears an
					// [isengard] condition.
					put("traitor", "6_72");
					// Saruman, Servant of the Eye: the [isengard] Saruman, a Wizard, vitality 4.
					put("saruman", "3_69");
					// Saruman, Rabble-rouser: the [dunland] Saruman, a Wizard, vitality 4; his move trigger has
					// nothing to count, being laid after the move.
					put("rabblerouser", "4_33");
					// Saruman, Servant of Sauron: the [isengard] follower Saruman, no character; a skirmish-win
					// trigger only.
					put("follower", "17_38");
					// Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text.
					put("savage", "1_151");
					// Dunlending Footmen: a [dunland] Man, vitality 2; a cost modifier in skirmishes only.
					put("footmen", "6_3");
					// Uruk Pursuer: an [isengard] Uruk-hai, strength 5, vitality 3, fierce; its texts are about
					// unbound Hobbits killed or discarded.
					put("pursuer", "4_190");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					// Aragorn, Ranger of the North: a [gondor] Man companion, strength 8, vitality 4; a maneuver
					// ability never used.
					put("aragorn", "1_89");
					// Boromir, Son of Denethor: a [gondor] Man companion, strength 7, vitality 3; a skirmish
					// ability never used.
					put("boromir", "1_97");
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion, strength 3, vitality 4; +2 only
					// while he bears a weapon.
					put("merry", "1_303");
					// Guma, Plains Farmer: a [rohan] Man ally, home 4T; discarded only if an opponent controls
					// that site, which is on no path here.
					put("guma", "4_277");
					// Elrond, Lord of Rivendell: an [elven] Elf ally, home site 3, strength 8, vitality 4.
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BanishedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Banished
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert Saruman or an [isengard] Man. Plays on a Free Peoples Man.<br>Each time another ally or companion loses a skirmish, bearer must exert.
		*/

		var scn = GetScenario();

		var banished = scn.GetFreepsCard("banished");

		assertEquals("Banished", banished.getBlueprint().getTitle());
		assertNull(banished.getBlueprint().getSubtitle());
		assertFalse(banished.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, banished.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, banished.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, banished.getBlueprint().getCardType());
		assertEquals(0, banished.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedByExertingAnIsengardManOnAFreePeoplesMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banished = scn.GetShadowCard("banished");
		var grima = scn.GetShadowCard("grima");
		var traitor = scn.GetShadowCard("traitor");
		var savage = scn.GetShadowCard("savage");
		var footmen = scn.GetShadowCard("footmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(banished);

		scn.StartGame();
		assertInZone(Zone.HAND, banished);
		scn.FreepsPass();
		scn.MoveMinionsToTable(grima, traitor, savage, footmen);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.HAND, banished);
		assertTrue(scn.ShadowPlayAvailable(banished));
		scn.ShadowPlayCard(banished);

		// The bearer is chosen first.  A Free Peoples Man companion and a Free Peoples Man ally of another
		// culture; not a Free Peoples Hobbit, nor the Shadow player's own Man.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, guma));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(merry, grima));
		scn.ShadowChooseCard(aragorn);

		// Then the cost: two [isengard] Men of different titles; not an [isengard] Uruk-hai, nor a [dunland]
		// Man, though both could exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(grima, traitor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, footmen));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(traitor));
		scn.ShadowChooseCard(traitor);
		assertEquals(1, scn.GetWoundsOn(traitor));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(footmen));

		assertAttachedTo(banished, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSarumanAndAnIsengardManBothAbleToExertEitherMayPayAndSarumanIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banished = scn.GetShadowCard("banished");
		var saruman = scn.GetShadowCard("saruman");
		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(banished);

		scn.StartGame();
		assertInZone(Zone.HAND, banished);
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, grima);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.HAND, banished);
		scn.ShadowPlayCard(banished);

		// Both halves of the "or" can be paid: the one choice offers each.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, grima));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(grima));

		// Aragorn is the only Free Peoples Man: the condition goes on him unasked.
		assertAttachedTo(banished, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDunlandSarumanPaysTheCostAsSaruman() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banished = scn.GetShadowCard("banished");
		var rabblerouser = scn.GetShadowCard("rabblerouser");
		var traitor = scn.GetShadowCard("traitor");
		var footmen = scn.GetShadowCard("footmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(banished);

		scn.StartGame();
		assertInZone(Zone.HAND, banished);
		// Laid after the move, so that Saruman's own move trigger has nothing to count.
		scn.FreepsPass();
		scn.MoveMinionsToTable(rabblerouser, traitor, footmen);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.HAND, banished);
		scn.ShadowPlayCard(banished);

		// The [dunland] Saruman by his title, beside an [isengard] Man; not a [dunland] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rabblerouser, traitor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(footmen));
		assertEquals(0, scn.GetWoundsOn(rabblerouser));
		scn.ShadowChooseCard(rabblerouser);
		assertEquals(1, scn.GetWoundsOn(rabblerouser));
		assertEquals(0, scn.GetWoundsOn(traitor));
		assertEquals(0, scn.GetWoundsOn(footmen));

		assertAttachedTo(banished, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyIsengardManIsExhaustedAndTheOnlySarumanIsAFollower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banished = scn.GetShadowCard("banished");
		var follower = scn.GetShadowCard("follower");
		var grima = scn.GetShadowCard("grima");
		var savage = scn.GetShadowCard("savage");
		var footmen = scn.GetShadowCard("footmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(banished);

		scn.StartGame();
		assertInZone(Zone.HAND, banished);
		scn.FreepsPass();
		scn.MoveMinionsToTable(grima, savage, footmen);
		scn.MoveCardsToSupportArea(follower);
		// Vitality 3 with 2 wounds: Gríma cannot exert.  The Uruk-hai and the [dunland] Man could.  The one
		// Saruman in play is a follower, which is no character and cannot exert.
		scn.AddWoundsToChar(grima, 2);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SUPPORT, follower);
		assertEquals(2, scn.GetWoundsOn(grima));
		assertInZone(Zone.HAND, banished);
		assertFalse(scn.ShadowPlayAvailable(banished));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeAnotherCompanionOrAnAllyLosesASkirmishBearerExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banished = scn.GetShadowCard("banished");
		var pursuer = scn.GetShadowCard("pursuer");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, boromir, merry);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AttachCardsTo(aragorn, banished);
		assertAttachedTo(banished, aragorn);
		// The move to site 3, Elrond's home, where he may be assigned.
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer, troop);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, pursuer}, new PhysicalCardImpl[]{elrond, troop});

		// Merry, a companion, loses (3 against 5): Aragorn exerts.  Boromir, a Free Peoples Man who does not
		// bear it, does not.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		// Elrond, an ally, loses (8 against 13): Aragorn exerts again.
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsResolveSkirmish(elrond);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The Pursuer is fierce: Merry loses to it a second time, and Aragorn exerts a third time.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(merry, pursuer);
		scn.FreepsResolveSkirmish(merry);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NeitherBearerLosingNorAnotherCompanionWinningMakesBearerExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banished = scn.GetShadowCard("banished");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, banished);

		scn.StartGame();
		assertAttachedTo(banished, aragorn);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{boromir, savage});

		// The bearer himself loses (8 against 13): the wound for losing and no exertion.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Boromir wins (7 against 5): a minion loses, and the bearer does not exert.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
