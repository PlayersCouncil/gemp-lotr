package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("traveler", "17_85");

					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc, strength 9; +2 only at an underground site
					put("denizen2", "11_115");
					put("vicious", "17_92");      // Vicious Warg: [orc] mount, cost 3, +4 strength, fierce; bearer an [orc] Orc of strength 9 or less
					put("relentless", "17_89");   // Relentless Warg: [orc] mount, cost 3; its play trigger never reached (it stays in hand)
					put("threatening", "17_91");  // Threatening Warg: [orc] mount, +2 strength, fierce; cheated on, so its play trigger is never set off
					put("gear", "17_90");         // Rider's Gear: an [orc] possession that is no mount (armor), cost 1, +2 strength
					put("warwarg", "5_64");       // War-warg: an [isengard] mount, cost 4, +4 strength, for a warg-rider; its response needs bearer in the skirmish
					put("wargrider", "5_67");     // Warg-rider: [isengard] warg-rider Orc, strength 5; fierce while not exhausted

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: 6, +1 for a Man and +1 for a Hobbit here: 8
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishTravelerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Traveler
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each [orc] mount is twilight cost -1.<br><b>Skirmish:</b> Remove (3) to make a mounted [orc] Orc strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("traveler");

		assertEquals("Orkish Traveler", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachOrcMountIsTwilightCostMinusOneWhileTheTravelerIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveler = scn.GetShadowCard("traveler");
		var denizen = scn.GetShadowCard("denizen");
		var vicious = scn.GetShadowCard("vicious");
		var relentless = scn.GetShadowCard("relentless");
		var gear = scn.GetShadowCard("gear");
		var warwarg = scn.GetShadowCard("warwarg");

		scn.MoveCardsToHand(traveler, vicious, relentless, gear, warwarg);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen);
		assertInHand(traveler, vicious, relentless, gear, warwarg);

		// The Traveler in hand changes nothing.
		assertEquals(3, scn.GetTwilightCost(vicious));
		assertEquals(3, scn.GetTwilightCost(relentless));
		assertEquals(1, scn.GetTwilightCost(gear));
		assertEquals(4, scn.GetTwilightCost(warwarg));

		scn.MoveMinionsToTable(traveler);
		// Two [orc] mounts of two titles are 1 less; an [orc] possession that is no mount and an [isengard] mount
		// are not.
		assertEquals(2, scn.GetTwilightCost(vicious));
		assertEquals(2, scn.GetTwilightCost(relentless));
		assertEquals(1, scn.GetTwilightCost(gear));
		assertEquals(4, scn.GetTwilightCost(warwarg));

		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(10, scn.GetTwilight());
		scn.ShadowPlayCard(vicious);
		scn.ShadowChooseCard(denizen);

		assertAttachedTo(vicious, denizen);
		// 10 - (3 - 1)
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemoves3ToMakeAMountedOrcOrcStrengthPlus2UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var traveler = scn.GetShadowCard("traveler");
		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var vicious = scn.GetShadowCard("vicious");
		var threatening = scn.GetShadowCard("threatening");
		var gear = scn.GetShadowCard("gear");
		var warwarg = scn.GetShadowCard("warwarg");
		var wargrider = scn.GetShadowCard("wargrider");

		scn.StartGame();
		scn.MoveMinionsToTable(traveler, denizen, denizen2, wargrider);
		// Mounted [orc] Orcs of two titles; an [orc] Orc bearing a possession that is no mount; a mounted
		// [isengard] Orc.
		scn.AttachCardsTo(traveler, vicious);
		scn.AttachCardsTo(denizen, threatening);
		scn.AttachCardsTo(denizen2, gear);
		scn.AttachCardsTo(wargrider, warwarg);
		assertAttachedTo(vicious, traveler);
		assertAttachedTo(threatening, denizen);
		assertAttachedTo(gear, denizen2);
		assertAttachedTo(warwarg, wargrider);

		scn.SkipToAssignments();
		// The mounted Denizen and the Warg-rider are left out of every skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, traveler}, new PhysicalCardImpl[]{gandalf, denizen2});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(11, scn.GetStrength(traveler));    // 7 + 4
		assertEquals(11, scn.GetStrength(denizen));     // 9 + 2
		assertEquals(11, scn.GetStrength(denizen2));    // 9 + 2
		assertEquals(9, scn.GetStrength(wargrider));    // 5 + 4
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(traveler));
		scn.ShadowUseCardAction(traveler);

		assertEquals(2, scn.GetTwilight());
		// A mounted [orc] Orc, in this skirmish or in none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(traveler, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(denizen2, wargrider));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(traveler);

		assertEquals(13, scn.GetStrength(traveler));
		assertEquals(11, scn.GetStrength(denizen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still +2 at the end of its skirmish: 13 against 8.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Each skirmish is a phase of its own: in the next one the +2 is gone.
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(11, scn.GetStrength(traveler));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithOnly2TwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var traveler = scn.GetShadowCard("traveler");
		var vicious = scn.GetShadowCard("vicious");

		scn.StartGame();
		scn.MoveMinionsToTable(traveler);
		scn.AttachCardsTo(traveler, vicious);
		assertAttachedTo(vicious, traveler);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, traveler);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(traveler));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(traveler));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
